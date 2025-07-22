package cn.lunadeer.mc.timeManager.manager;

import cn.lunadeer.mc.timeManager.utils.XLogger;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeManager {
    private static final Map<UUID, Boolean> timeOverridden = new HashMap<>();
    private static final Map<UUID, Boolean> timeFrozen = new HashMap<>();

    public static void setPlayerTime(Player player, long time) {
        UUID uuid = player.getUniqueId();
        timeOverridden.put(uuid, true);
        timeFrozen.remove(uuid);

        long serverTime = player.getWorld().getTime();
        serverTime = serverTime % 24000;

        long offset = time - serverTime;
        player.setPlayerTime(offset, true);
        XLogger.debug("Set player {0} time to {1}", player.getName(), time);
    }

    public static void freezePlayerTime(Player player) {
        UUID uuid = player.getUniqueId();

        long frozenTime;
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

    public static void unfreezePlayerTime(Player player) {
        UUID uuid = player.getUniqueId();
        timeFrozen.remove(uuid);
        player.setPlayerTime(0, true);

        XLogger.debug("Unfroze player {0} time", player.getName());
    }

    public static void resetPlayerTime(Player player) {
        UUID uuid = player.getUniqueId();
        cleanupPlayer(uuid);

        player.resetPlayerTime();
        XLogger.debug("Reset player {0} time to server time", player.getName());
    }

    public static boolean isTimeOverridden(Player player) {
        return timeOverridden.getOrDefault(player.getUniqueId(), false);
    }


    public static boolean isTimeFrozen(Player player) {
        return timeFrozen.getOrDefault(player.getUniqueId(), false);
    }

    public static void cleanupPlayer(UUID uuid) {
        timeOverridden.remove(uuid);
        timeFrozen.remove(uuid);
    }

}
