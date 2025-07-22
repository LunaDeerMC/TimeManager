package cn.lunadeer.mc.timeManager.utils;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import static cn.lunadeer.mc.timeManager.utils.Misc.formatString;

public class XLogger {
    public static XLogger instance;

    public XLogger(@NotNull JavaPlugin plugin) {
        instance = this;
        this.sender = plugin.getServer().getConsoleSender();
    }

    public static XLogger setDebug(boolean debug) {
        instance.debug = debug;
        return instance;
    }

    public static boolean isDebug() {
        return instance.debug;
    }

    private final ConsoleCommandSender sender;
    private boolean debug = false;

    public static void info(String message) {
        Notification.info(instance.sender, "&a I | " + message);
    }

    public static void warn(String message) {
        Notification.warn(instance.sender, "&e W | " + message);
    }

    public static void error(String message) {
        Notification.error(instance.sender, "&c E | " + message);
    }

    public static void debug(String message) {
        if (!instance.debug) return;
        Notification.info(instance.sender, "&9 D | " + message);
    }

    public static void info(String message, Object... args) {
        info(formatString(message, args));
    }

    public static void warn(String message, Object... args) {
        warn(formatString(message, args));
    }

    public static void error(String message, Object... args) {
        error(formatString(message, args));
    }

    public static void error(Throwable e) {
        error(e.getMessage());
        if (isDebug()) {
            for (StackTraceElement element : e.getStackTrace()) {
                error("StackTrace | " + element.toString());
            }
        }
    }

    public static void debug(String message, Object... args) {
        debug(formatString(message, args));
    }
}
