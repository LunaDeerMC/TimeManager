package cn.lunadeer.mc.timeEssence.manager;

import cn.lunadeer.mc.timeEssence.Configuration;
import cn.lunadeer.mc.timeEssence.utils.XLogger;
import cn.lunadeer.mc.timeEssence.utils.scheduler.CancellableTask;
import cn.lunadeer.mc.timeEssence.utils.scheduler.Scheduler;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeManager {
    private static final Map<UUID, Long> playerTimes = new HashMap<>();
    private static final Map<UUID, Boolean> timeOverridden = new HashMap<>();
    private static final Map<UUID, Boolean> timeFrozen = new HashMap<>();
    private static CancellableTask updateTask;

    // NMS相关的反射缓存 - 针对1.21.1+优化
    private static Class<?> craftPlayerClass;
    private static Class<?> serverPlayerClass;
    private static Class<?> clientboundSetTimePacketClass;
    private static Class<?> serverGamePacketListenerImplClass;
    private static Method getHandleMethod;
    private static Field connectionField;
    private static Method sendMethod;
    private static Constructor<?> packetConstructor;
    private static boolean nmsInitialized = false;
    private static String serverVersion;

    static {
        initializeNMS();
    }

    private static void initializeNMS() {
        try {
            // 获取服务器版本
            String packageName = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
            serverVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
            XLogger.debug("Detected server version: {0}", serverVersion);

            // 对于某些服务器实现（如Folia），版本可能是 "craftbukkit"
            if ("craftbukkit".equals(serverVersion)) {
                // 尝试从服务器版本字符串中获取更准确的版本信息
                String bukkitVersion = org.bukkit.Bukkit.getBukkitVersion();
                XLogger.debug("Bukkit version string: {0}", bukkitVersion);

                // 从 "1.21.6-DEV-bea9fb3" 这样的字符串中提取版本
                if (bukkitVersion.startsWith("1.21")) {
                    serverVersion = "v1_21_R1"; // 使用标准格式
                    XLogger.debug("Adjusted server version to: {0}", serverVersion);
                } else {
                    XLogger.warn("Could not determine exact version from: {0}, assuming v1_21_R1", bukkitVersion);
                    serverVersion = "v1_21_R1";
                }
            }

            // 验证版本是否为1.21+
            if (!isSupported121Plus()) {
                throw new UnsupportedOperationException("This plugin only supports Minecraft 1.21+ and above. Current version: " + serverVersion);
            }

            // 初始化现代NMS (1.21+)
            initializeModernNMS();

            nmsInitialized = true;
            XLogger.info("NMS initialization successful for version {0}", serverVersion);

        } catch (Exception e) {
            XLogger.error("Failed to initialize NMS for version {0}: {1}", serverVersion, e);
            nmsInitialized = false;
        }
    }

    private static boolean isSupported121Plus() {
        try {
            String[] versionParts = serverVersion.split("_");
            if (versionParts.length >= 2) {
                int major = Integer.parseInt(versionParts[1]);
                int minor = versionParts.length >= 3 ? Integer.parseInt(versionParts[2]) : 0;

                // 检查是否为1.21+
                if (major > 21) return true;
                if (major == 21 && minor >= 1) return true;
                if (major == 21 && minor == 0) {
                    // 对于1.21.0，也可以支持，因为NMS结构类似
                    XLogger.warn("Version 1.21.0 detected, this plugin is optimized for 1.21.1+");
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            XLogger.debug("Failed to parse version, assuming modern version: {0}", e);
        }

        // 如果解析失败，检查是否能找到现代NMS类
        try {
            Class.forName("net.minecraft.server.level.ServerPlayer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void initializeModernNMS() throws Exception {
        // 对于DeerFolia，尝试不同的NMS路径
        Exception lastException = null;

        // 尝试标准的CraftBukkit路径
        try {
            craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + serverVersion + ".entity.CraftPlayer");
        } catch (ClassNotFoundException e) {
            lastException = e;
            // 尝试不带版本的路径（某些Folia分支可能使用这种格式）
            try {
                craftPlayerClass = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            } catch (ClassNotFoundException e2) {
                throw new ClassNotFoundException("Could not find CraftPlayer class. Tried: " +
                        "org.bukkit.craftbukkit." + serverVersion + ".entity.CraftPlayer and " +
                        "org.bukkit.craftbukkit.entity.CraftPlayer", e2);
            }
        }

        // 使用现代包结构 (1.17+)
        serverPlayerClass = Class.forName("net.minecraft.server.level.ServerPlayer");
        clientboundSetTimePacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetTimePacket");
        serverGamePacketListenerImplClass = Class.forName("net.minecraft.server.network.ServerGamePacketListenerImpl");

        // 初始化方法和字段
        getHandleMethod = craftPlayerClass.getMethod("getHandle");

        // 在1.21.1+中，连接字段名为 "connection"
        connectionField = serverPlayerClass.getField("connection");

        // 发包方法名为 "send"
        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.Packet");
        sendMethod = serverGamePacketListenerImplClass.getMethod("send", packetClass);

        // 时间数据包构造函数 (worldAge, dayTime, doDaylightCycle)
        packetConstructor = clientboundSetTimePacketClass.getConstructor(long.class, long.class, boolean.class);

        XLogger.debug("Modern NMS classes loaded successfully");
    }

    /**
     * 设置玩家的个人时间
     */
    public static void setPlayerTime(Player player, long time) {
        if (!nmsInitialized) {
            XLogger.error("NMS not initialized, cannot set player time");
            return;
        }

        UUID uuid = player.getUniqueId();
        playerTimes.put(uuid, time);
        timeOverridden.put(uuid, true);
        // 设置新时间时解除冻结状态
        timeFrozen.remove(uuid);

        sendTimePacket(player, time);
        XLogger.debug("Set player {0} time to {1}", player.getName(), time);
    }

    /**
     * 冻结玩家的当前时间
     */
    public static void freezePlayerTime(Player player) {
        if (!nmsInitialized) {
            XLogger.error("NMS not initialized, cannot freeze player time");
            return;
        }

        UUID uuid = player.getUniqueId();

        // 如果玩家当前没有自定义时间，使用当前服务器时间
        if (!timeOverridden.getOrDefault(uuid, false)) {
            long currentTime = player.getWorld().getTime();
            playerTimes.put(uuid, currentTime);
            timeOverridden.put(uuid, true);
        }

        timeFrozen.put(uuid, true);

        // 发送当前时间数据包确保时间被冻结
        Long frozenTime = playerTimes.get(uuid);
        if (frozenTime != null) {
            sendTimePacket(player, frozenTime);
        }

        XLogger.debug("Froze player {0} time at {1}", player.getName(), frozenTime);
    }

    /**
     * 解冻玩家的时间
     */
    public static void unfreezePlayerTime(Player player) {
        UUID uuid = player.getUniqueId();
        timeFrozen.remove(uuid);

        XLogger.debug("Unfroze player {0} time", player.getName());
    }

    /**
     * 重置玩家时间为服务器时间
     */
    public static void resetPlayerTime(Player player) {
        UUID uuid = player.getUniqueId();
        playerTimes.remove(uuid);
        timeOverridden.remove(uuid);
        timeFrozen.remove(uuid);

        // 发送服务器当前时间
        long serverTime = player.getWorld().getTime();
        sendTimePacket(player, serverTime);
        XLogger.debug("Reset player {0} time to server time", player.getName());
    }

    /**
     * 获取玩家的当前时间设置
     */
    public static Long getPlayerTime(Player player) {
        return playerTimes.get(player.getUniqueId());
    }

    /**
     * 检查玩家是否覆盖了时间
     */
    public static boolean isTimeOverridden(Player player) {
        return timeOverridden.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * 检查玩家时间是否被冻结
     */
    public static boolean isTimeFrozen(Player player) {
        return timeFrozen.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * 发送时间数据包给玩家 - 优化版本，专门针对1.21.1+
     */
    private static void sendTimePacket(Player player, long time) {
        if (!nmsInitialized) {
            return;
        }

        try {
            // 获取ServerPlayer
            Object craftPlayer = craftPlayerClass.cast(player);
            Object serverPlayer = getHandleMethod.invoke(craftPlayer);

            // 获取ServerGamePacketListenerImpl (connection)
            Object connection = connectionField.get(serverPlayer);

            // 创建ClientboundSetTimePacket
            // 参数: worldAge, dayTime, doDaylightCycle
            // worldAge使用相同的时间值，dayTime是实际显示时间，doDaylightCycle设为false防止客户端自动更新时间
            Object packet = packetConstructor.newInstance(time, time, false);

            // 发送数据包
            sendMethod.invoke(connection, packet);

        } catch (Exception e) {
            XLogger.error("Failed to send time packet to player {0}: {1}", player.getName(), e);
        }
    }

    /**
     * 启动时间更新任务
     */
    public static void startUpdateTask() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
        updateTask = Scheduler.runTaskRepeatAsync(TimeManager::updatePlayerTimes, 1L, 1L);
    }

    /**
     * 更新所有玩家的时间
     */
    private static void updatePlayerTimes() {
        for (Map.Entry<UUID, Long> entry : new HashMap<>(playerTimes).entrySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                // 检查玩家时间是否被冻结
                if (!timeFrozen.getOrDefault(entry.getKey(), false)) {
                    // 只有未冻结的玩家才更新时间
                    long newTime = entry.getValue() + 1;
                    playerTimes.put(player.getUniqueId(), newTime);
                    sendTimePacket(player, newTime);
                } else {
                    // 冻结的玩家保持当前时间不变
                    sendTimePacket(player, entry.getValue());
                }
            } else {
                cleanupPlayer(entry.getKey());
            }
        }
    }

    /**
     * 停止时间更新任务
     */
    public static void stopUpdateTask() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
            XLogger.debug("Time update task stopped");
        }
    }

    /**
     * 清理玩家数据
     */
    public static void cleanupPlayer(UUID uuid) {
        playerTimes.remove(uuid);
        timeOverridden.remove(uuid);
        timeFrozen.remove(uuid);
    }

    /**
     * 获取所有有自定义时间的玩家数量
     */
    public static int getOverriddenPlayersCount() {
        return playerTimes.size();
    }

    /**
     * 检查NMS是否已初始化
     */
    public static boolean isNMSInitialized() {
        return nmsInitialized;
    }

    /**
     * 获取服务器版本
     */
    public static String getServerVersion() {
        return serverVersion;
    }
}
