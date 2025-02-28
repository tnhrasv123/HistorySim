package me.ThePCUser.historySim.commands;

import me.ThePCUser.historySim.HistorySim;
import me.ThePCUser.historySim.eras.EraType;
import me.ThePCUser.historySim.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Command handler for admin commands
 */
public class AdminCommands implements CommandExecutor {

    private final HistorySim plugin;

    public AdminCommands(HistorySim plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("historysim.admin")) {
            MessageUtils.sendMessage(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            showAdminHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "setplayerera":
                if (args.length < 3) {
                    MessageUtils.sendMessage(sender, "Usage: /historyadmin setplayerera <player> <era>");
                    return true;
                }
                setPlayerEra(sender, args[1], args[2]);
                break;
            case "triggerevent":
                if (args.length < 2) {
                    MessageUtils.sendMessage(sender, "Usage: /historyadmin triggerevent <eventId> [world]");
                    return true;
                }
                triggerEvent(sender, args[1], args.length > 2 ? args[2] : null);
                break;
            case "reload":
                reloadPlugin(sender);
                break;
            default:
                showAdminHelp(sender);
                break;
        }

        return true;
    }

    private void showAdminHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "§6===== History Simulator Admin Commands =====");
        MessageUtils.sendMessage(sender, "§e/historyadmin setplayerera <player> <era> §7- Set a player's era");
        MessageUtils.sendMessage(sender, "§e/historyadmin triggerevent <eventId> [world] §7- Trigger a historical event");
        MessageUtils.sendMessage(sender, "§e/historyadmin reload §7- Reload the plugin configuration");
    }

    private void setPlayerEra(CommandSender sender, String playerName, String eraName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            MessageUtils.sendMessage(sender, "Player not found or not online.");
            return;
        }

        EraType era;
        try {
            era = EraType.valueOf(eraName.toUpperCase());
        } catch (IllegalArgumentException e) {
            MessageUtils.sendMessage(sender, "Invalid era type. Available eras: " +
                    String.join(", ", Arrays.stream(EraType.values())
                            .map(EraType::name)
                            .collect(Collectors.toList())));
            return;
        }

        boolean success = plugin.changePlayerEra(targetPlayer, era);
        if (success) {
            MessageUtils.sendMessage(sender, "Player " + targetPlayer.getName() + " has been moved to the " + era.getDisplayName() + ".");
        } else {
            MessageUtils.sendMessage(sender, "Failed to change player's era. Check console for details.");
        }
    }

    private void triggerEvent(CommandSender sender, String eventId, String worldName) {
        World world = null;
        if (worldName != null) {
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                MessageUtils.sendMessage(sender, "World not found: " + worldName);
                return;
            }
        } else if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else {
            MessageUtils.sendMessage(sender, "You must specify a world when using this command from console.");
            return;
        }

        boolean success = plugin.getEventManager().triggerEvent(eventId, world);
        if (success) {
            MessageUtils.sendMessage(sender, "Event " + eventId + " has been triggered in world " + world.getName() + ".");
        } else {
            MessageUtils.sendMessage(sender, "Failed to trigger event. Check if the event ID exists.");
        }
    }

    private void reloadPlugin(CommandSender sender) {
        plugin.getConfigManager().reloadAllConfigs();
        MessageUtils.sendMessage(sender, "History Simulator configuration has been reloaded.");
    }
}
