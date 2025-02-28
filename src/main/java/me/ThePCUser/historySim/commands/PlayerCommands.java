package me.ThePCUser.historySim.commands;

import me.ThePCUser.historySim.eras.EraType;
import me.ThePCUser.historySim.HistorySim;
import me.ThePCUser.historySim.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for player commands
 */
public class PlayerCommands implements CommandExecutor {

    private final HistorySim plugin;

    public PlayerCommands(HistorySim plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "info":
                showEraInfo(player);
                break;
            case "status":
                showPlayerStatus(player);
                break;
            case "techs":
            case "technologies":
                showTechnologies(player);
                break;
            case "progress":
                showProgressionPath(player);
                break;
            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        MessageUtils.sendMessage(player, "§6===== History Simulator Commands =====");
        MessageUtils.sendMessage(player, "§e/historyera info §7- Show information about your current era");
        MessageUtils.sendMessage(player, "§e/historyera status §7- Display your current status and progress");
        MessageUtils.sendMessage(player, "§e/historyera techs §7- Show available technologies in your era");
        MessageUtils.sendMessage(player, "§e/historyera progress §7- Show progression path to next era");
    }

    private void showEraInfo(Player player) {
        EraType currentEra = plugin.getPlayerEra(player.getUniqueId());
        MessageUtils.sendMessage(player, "§6===== " + currentEra.getDisplayName() + " =====");

        switch (currentEra) {
            case PREHISTORIC:
                MessageUtils.sendMessage(player, "§7Period: 10,000 BCE - 3,000 BCE");
                MessageUtils.sendMessage(player, "§7Key features: Stone tools, hunter-gatherer lifestyle, early agriculture");
                MessageUtils.sendMessage(player, "§7Goal: Develop agriculture and early settlements to advance to the Ancient Era");
                break;
            case ANCIENT:
                MessageUtils.sendMessage(player, "§7Period: 3,000 BCE - 500 CE");
                MessageUtils.sendMessage(player, "§7Key features: Bronze/iron working, early cities, writing systems");
                MessageUtils.sendMessage(player, "§7Goal: Build advanced cities and develop sophisticated crafting");
                break;
            case MEDIEVAL:
                MessageUtils.sendMessage(player, "§7Period: 500 CE - 1500 CE");
                MessageUtils.sendMessage(player, "§7Key features: Feudal system, castles, guilds, religious institutions");
                MessageUtils.sendMessage(player, "§7Goal: Create fortified structures and establish trade networks");
                break;
            // Add remaining eras
            default:
                MessageUtils.sendMessage(player, "§7Information not available for this era.");
                break;
        }
    }

    private void showPlayerStatus(Player player) {
        UUID playerId = player.getUniqueId();
        EraType currentEra = plugin.getPlayerEra(playerId);

        MessageUtils.sendMessage(player, "§6===== Your Status =====");
        MessageUtils.sendMessage(player, "§7Current Era: §e" + currentEra.getDisplayName());

        // Get player's technologies
        List<String> technologies = plugin.getDatabaseManager().getPlayerTechnologies(playerId);
        MessageUtils.sendMessage(player, "§7Technologies Unlocked: §e" + technologies.size());

        // Get progression to next era
        int progress = plugin.getDatabaseManager().getEraProgressionPercentage(playerId, currentEra);
        MessageUtils.sendMessage(player, "§7Progress to next era: §e" + progress + "%");
    }

    private void showTechnologies(Player player) {
        UUID playerId = player.getUniqueId();
        EraType currentEra = plugin.getPlayerEra(playerId);

        MessageUtils.sendMessage(player, "§6===== Available Technologies =====");

        List<Technology> availableTechs = plugin.getEra(currentEra).getTechManager().getAvailableTechnologies(playerId);

        if (availableTechs.isEmpty()) {
            MessageUtils.sendMessage(player, "§7No technologies currently available.");
            return;
        }

        for (Technology tech : availableTechs) {
            boolean unlocked = plugin.getDatabaseManager().hasPlayerTechnology(playerId, tech.getId());
            String status = unlocked ? "§a[UNLOCKED]" : "§e[AVAILABLE]";

            MessageUtils.sendMessage(player, status + " §7" + tech.getName() + " - " + tech.getDescription());
        }
    }

    private void showProgressionPath(Player player) {
        UUID playerId = player.getUniqueId();
        EraType currentEra = plugin.getPlayerEra(playerId);

        // Get next era if not already at the last one
        EraType nextEra = null;
        EraType[] eras = EraType.values();
        for (int i = 0; i < eras.length - 1; i++) {
            if (eras[i] == currentEra) {
                nextEra = eras[i+1];
                break;
            }
        }

        if (nextEra == null) {
            MessageUtils.sendMessage(player, "§7You have reached the final era!");
            return;
        }

        MessageUtils.sendMessage(player, "§6===== Path to " + nextEra.getDisplayName() + " =====");

        // Get required milestones
        List<String> requirements = plugin.getEra(currentEra).getAdvancementRequirements();
        List<String> completed = plugin.getDatabaseManager().getCompletedMilestones(playerId, currentEra);

        for (String req : requirements) {
            boolean isComplete = completed.contains(req);
            String status = isComplete ? "§a[COMPLETE]" : "§c[INCOMPLETE]";
            MessageUtils.sendMessage(player, status + " §7" + req);
        }

        int progress = plugin.getDatabaseManager().getEraProgressionPercentage(playerId, currentEra);
        MessageUtils.sendMessage(player, "§7Overall Progress: §e" + progress + "%");
    }
}