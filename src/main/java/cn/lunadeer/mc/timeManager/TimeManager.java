package cn.lunadeer.mc.timeManager;

import cn.lunadeer.mc.timeManager.commands.TimeCommand;
import cn.lunadeer.mc.timeManager.listeners.PlayerListener;
import cn.lunadeer.mc.timeManager.utils.Notification;
import cn.lunadeer.mc.timeManager.utils.XLogger;
import cn.lunadeer.mc.timeManager.utils.configuration.ConfigurationManager;
import cn.lunadeer.mc.timeManager.utils.scheduler.Scheduler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class TimeManager extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        new Notification(this);
        new XLogger(this);
        new Scheduler(this);

        try {
            ConfigurationManager.load(Configuration.class, new File(TimeManager.getInstance().getDataFolder(), "config.yml"));
        } catch (Exception e) {
            XLogger.error("Failed to load configuration: {0}", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        XLogger.setDebug(Configuration.debug);

        // 注册指令
        if (Configuration.enableTimeCommand) {
            TimeCommand timeCommand = new TimeCommand();
            org.bukkit.command.PluginCommand timeCmd = getCommand("ptime");
            if (timeCmd != null) {
                timeCmd.setExecutor(timeCommand);
                timeCmd.setTabCompleter(timeCommand);
                XLogger.info("Time command registered successfully");
            } else {
                XLogger.error("Failed to register time command - command not found in plugin.yml");
            }
        }

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        XLogger.info("Event listeners registered successfully");

        XLogger.info("TimeManager plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        XLogger.info("TimeManager plugin disabled");
    }


    public static TimeManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TimeManager instance is not initialized. Make sure the plugin is enabled.");
        }
        return instance;
    }

    private static TimeManager instance;
}
