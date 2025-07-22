package cn.lunadeer.mc.timeEssence;

import cn.lunadeer.mc.timeEssence.commands.TimeCommand;
import cn.lunadeer.mc.timeEssence.listeners.PlayerListener;
import cn.lunadeer.mc.timeEssence.utils.XLogger;
import cn.lunadeer.mc.timeEssence.utils.configuration.ConfigurationManager;
import cn.lunadeer.mc.timeEssence.utils.scheduler.Scheduler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class TimeEssence extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        new XLogger(this);
        new Scheduler(this);

        try {
            ConfigurationManager.load(Configuration.class, new File(TimeEssence.getInstance().getDataFolder(), "config.yml"));
        } catch (Exception e) {
            XLogger.error("Failed to load configuration: {0}", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        XLogger.setDebug(Configuration.debug);

        // 注册指令
        if (Configuration.enableTimeCommand) {
            TimeCommand timeCommand = new TimeCommand();
            org.bukkit.command.PluginCommand timeCmd = getCommand("time");
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

        XLogger.info("TimeEssence plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        XLogger.info("TimeEssence plugin disabled");
    }


    public static TimeEssence getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TimeEssence instance is not initialized. Make sure the plugin is enabled.");
        }
        return instance;
    }

    private static TimeEssence instance;
}
