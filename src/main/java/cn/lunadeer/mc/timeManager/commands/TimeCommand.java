package cn.lunadeer.mc.timeManager.commands;

import cn.lunadeer.mc.timeManager.manager.TimeManager;
import cn.lunadeer.mc.timeManager.utils.Notification;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Notification.error(sender, "This command can only be used by players!");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "set" -> handleSetCommand(player, args);
            case "reset" -> handleResetCommand(player);
            case "freeze" -> handleFreezeCommand(player);
            case "unfreeze" -> handleUnfreezeCommand(player);
            case "info" -> handleInfoCommand(player);
            case "help" -> {
                sendHelpMessage(player);
                yield true;
            }
            default -> {
                Notification.error(sender, "Unknown subcommand. Use /ptime help for help.");
                yield true;
            }
        };
    }

    private boolean handleSetCommand(Player player, String[] args) {
        if (!player.hasPermission("ptime.set")) {
            Notification.error(player, "You don't have permission to set your personal time!");
            return true;
        }
        if (args.length < 2) {
            Notification.error(player, "Usage: /ptime set <time>");
            Notification.error(player, "Time can be: day, night, noon, midnight, sunrise, sunset, or a number (0-24000)");
            return true;
        }

        long time;
        try {
            // 尝试解析预设时间
            time = parseTimeString(args[1]);
        } catch (NumberFormatException e) {
            Notification.error(player, "Invalid time format! Use: day, night, noon, midnight, sunrise, sunset, or a number (0-24000)");
            return true;
        } catch (IllegalArgumentException e) {
            Notification.error(player, e);
            return true;
        }

        TimeManager.setPlayerTime(player, time);
        Notification.info(player, "our personal time has been set to {0} ({1} ticks)", formatTime(time), time);
        return true;
    }

    private boolean handleResetCommand(Player player) {
        if (!TimeManager.isTimeOverridden(player)) {
            Notification.warn(player, "Your time is already synchronized with the server.");
            return true;
        }

        TimeManager.resetPlayerTime(player);
        long serverTime = player.getWorld().getTime();
        Notification.info(player, "Your time has been reset to server time: {0} ({1} ticks)", formatTime(serverTime), serverTime);

        return true;
    }

    private boolean handleFreezeCommand(Player player) {
        if (!player.hasPermission("ptime.freeze")) {
            Notification.error(player, "You don't have permission to set your personal time!");
            return true;
        }
        TimeManager.freezePlayerTime(player);
        Notification.info(player, "Your personal time has been frozen.");
        return true;
    }

    private boolean handleUnfreezeCommand(Player player) {
        if (!player.hasPermission("ptime.freeze")) {
            Notification.error(player, "You don't have permission to set your personal time!");
            return true;
        }
        TimeManager.unfreezePlayerTime(player);
        Notification.info(player, "Your personal time has been unfrozen.");
        return true;
    }

    private boolean handleInfoCommand(Player player) {
        long serverTime = player.getWorld().getTime();

        if (TimeManager.isTimeOverridden(player)) {
            Long playerTime = player.getPlayerTime();
            boolean isFrozen = TimeManager.isTimeFrozen(player);

            player.sendMessage(ChatColor.AQUA + "=== Time Information ===");
            player.sendMessage(ChatColor.YELLOW + "Server Time: " + ChatColor.WHITE + formatTime(serverTime) + " (" + serverTime + " ticks)");
            player.sendMessage(ChatColor.GREEN + "Your Time: " + ChatColor.WHITE + formatTime(playerTime) + " (" + playerTime + " ticks)");

            if (isFrozen) {
                player.sendMessage(ChatColor.GOLD + "Status: " + ChatColor.RED + "FROZEN");
                player.sendMessage(ChatColor.GRAY + "Your time is frozen and will not change.");
            } else {
                player.sendMessage(ChatColor.GOLD + "Status: " + ChatColor.GREEN + "OVERRIDDEN");
                player.sendMessage(ChatColor.GRAY + "Your time is currently overridden but not frozen.");
            }
        } else {
            player.sendMessage(ChatColor.AQUA + "=== Time Information ===");
            player.sendMessage(ChatColor.YELLOW + "Server Time: " + ChatColor.WHITE + formatTime(serverTime) + " (" + serverTime + " ticks)");
            player.sendMessage(ChatColor.GREEN + "Your Time: " + ChatColor.WHITE + "Synchronized with server");
            player.sendMessage(ChatColor.GOLD + "Status: " + ChatColor.GREEN + "SYNCHRONIZED");
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.AQUA + "=== TimeManager Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/ptime set <time>" + ChatColor.WHITE + " - Set your personal time");
        player.sendMessage(ChatColor.YELLOW + "/ptime reset" + ChatColor.WHITE + " - Reset to server time");
        player.sendMessage(ChatColor.YELLOW + "/ptime freeze" + ChatColor.WHITE + " - Freeze your personal time");
        player.sendMessage(ChatColor.YELLOW + "/ptime unfreeze" + ChatColor.WHITE + " - Unfreeze your personal time");
        player.sendMessage(ChatColor.YELLOW + "/ptime info" + ChatColor.WHITE + " - Show time information");
        player.sendMessage(ChatColor.YELLOW + "/ptime help" + ChatColor.WHITE + " - Show this help message");
        player.sendMessage(ChatColor.GRAY + "Available times: day, night, noon, midnight, sunrise, sunset, or 0-24000");
    }

    private long parseTimeString(String timeStr) throws NumberFormatException, IllegalArgumentException {
        switch (timeStr.toLowerCase()) {
            case "day":
                return 1000L; // 6:00 AM in Minecraft time
            case "sunrise":
                return 0L;
            case "noon":
                return 6000L;
            case "sunset":
                return 12000L;
            case "night":
                return 14000L; // 8:00 PM in Minecraft time
            case "midnight":
                return 18000L;
            default:
                long time = Long.parseLong(timeStr);
                if (time < 0 || time > 24000) {
                    throw new IllegalArgumentException("Time must be between 0 and 24000!");
                }
                return time;
        }
    }

    private String formatTime(long ticks) {
        // Minecraft时间转换为12小时制
        ticks = ticks % 24000; // 确保在0-24000范围内

        // Minecraft中，0 ticks = 6:00 AM
        int hours = (int) ((ticks + 6000) / 1000) % 24;
        int minutes = (int) (((ticks + 6000) % 1000) * 60 / 1000);

        String period = hours < 12 ? "AM" : "PM";
        int displayHours = hours == 0 ? 12 : (hours > 12 ? hours - 12 : hours);

        return String.format("%d:%02d %s", displayHours, minutes, period);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "reset", "freeze", "unfreeze", "info", "help"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(Arrays.asList("day", "night", "noon", "midnight", "sunrise", "sunset", "0", "6000", "12000", "18000"));
        }

        return completions;
    }
}
