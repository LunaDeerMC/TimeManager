package cn.lunadeer.mc.timeManager.listeners;

import cn.lunadeer.mc.timeManager.Configuration;
import cn.lunadeer.mc.timeManager.manager.TimeManager;
import cn.lunadeer.mc.timeManager.utils.XLogger;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 发送欢迎消息（如果启用）
        if (Configuration.showWelcomeMessage && event.getPlayer().hasPermission("timeessence.use")) {
            String message = ChatColor.translateAlternateColorCodes('&', Configuration.welcomeMessage);
            event.getPlayer().sendMessage(message);
        }

        XLogger.debug("Player {0} joined the server", event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 玩家离开时清理其时间数据
        TimeManager.cleanupPlayer(event.getPlayer().getUniqueId());
        XLogger.debug("Cleaned up time data for player {0}", event.getPlayer().getName());
    }
}
