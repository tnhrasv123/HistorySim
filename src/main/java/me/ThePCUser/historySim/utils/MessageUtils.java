package me.ThePCUser.historySim.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageUtils {

    public static void logInfo(String message) {
        Bukkit.getLogger().info(message);
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(message);
    }
}
