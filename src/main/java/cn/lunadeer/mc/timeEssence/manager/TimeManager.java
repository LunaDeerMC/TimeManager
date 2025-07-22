package cn.lunadeer.mc.timeEssence.manager;

import cn.lunadeer.mc.timeEssence.utils.XLogger;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeManager {
    private static final Map<UUID, Boolean> timeOverridden = new HashMap<>();
    private static final Map<UUID, Boolean> timeFrozen = new HashMap<>();

    /**
     * 设置玩家的个人时间
     */
    public static void setPlayerTime(Player player, long time) {
        UUID uuid = player.getUniqueId();
        timeOverridden.put(uuid, true);
        // 设置新时间时解除冻结状态
        timeFrozen.remove(uuid);

        long serverTime = player.getWorld().getTime();
        serverTime = serverTime % 24000; // 确保时间在0-23999范围内

        player.setPlayerTime(serverTime - time, true);
        XLogger.debug("Set player {0} time to {1}", player.getName(), time);
    }

    /**
     * 冻结玩家的当前时间
     */
    public static void freezePlayerTime(Player player) {
        UUID uuid = player.getUniqueId();

        long frozenTime;
        // 如果玩家当前没有自定义时间，使用当前服务器时间
        if (!timeOverridden.getOrDefault(uuid, false)) {
            timeOverridden.put(uuid, true);
            frozenTime = player.getWorld().getTime();
        } else {
            frozenTime = player.getPlayerTime();
        }

        timeFrozen.put(uuid, true);
        player.setPlayerTime(frozenTime, false);

        XLogger.debug("Froze player {0} time at {1}", player.getName(), frozenTime);
    }

    /**
     * 解冻玩家的时间
     */
    public static void unfreezePlayerTime(Player player) {
        UUID uuid = player.getUniqueId();
        timeFrozen.remove(uuid);
        player.setPlayerTime(0, true);

        XLogger.debug("Unfroze player {0} time", player.getName());
    }

    /**
     * 重置玩家时间为服务器时间
     */
    public static void resetPlayerTime(Player player) {
        UUID uuid = player.getUniqueId();
        cleanupPlayer(uuid);

        // 发送服务器当前时间
        player.resetPlayerTime();
        XLogger.debug("Reset player {0} time to server time", player.getName());
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
     * 清理玩家数据
     */
    public static void cleanupPlayer(UUID uuid) {
        timeOverridden.remove(uuid);
        timeFrozen.remove(uuid);
    }

}
