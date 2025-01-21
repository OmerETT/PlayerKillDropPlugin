package com.PlayerKillDrop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PlayerKillDropPlugin extends JavaPlugin implements Listener {

    private boolean pluginEnabled; // Tracks if the plugin is globally enabled
    private List<String> enabledWorlds; // List of worlds where the plugin is active
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Save default config.yml if it doesn't exist
        saveDefaultConfig();
        config = getConfig();

        // Load initial plugin state from config
        pluginEnabled = config.getBoolean("enabled", true);
        enabledWorlds = config.getStringList("enabledWorlds");

        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, this);

        // Register commands
        registerCommands();

        getLogger().info("PlayerKillDropPlugin v1.1-SNAPSHOT has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PlayerKillDropPlugin v1.1-SNAPSHOT has been disabled!");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!pluginEnabled) {
            return; // Do nothing if the plugin is disabled
        }

        Player player = event.getEntity(); // The player who died
        String worldName = player.getWorld().getName();

        if (!enabledWorlds.contains(worldName)) {
            return; // Do nothing if the world is not enabled
        }

        Player killer = player.getKiller(); // The player who killed them, if any

        if (killer instanceof Player) {
            // If killed by another player, items drop normally
            event.setKeepInventory(false); // Disable inventory retention
        } else {
            // If not killed by a player, keep their inventory
            event.setKeepInventory(true); // Enable inventory retention
            event.getDrops().clear(); // Clear default drops to prevent duplication
        }
    }

    private void registerCommands() {
        // Command to toggle the plugin on/off
        getCommand("togglekilldrop").setExecutor((sender, command, label, args) -> {
            if (sender.hasPermission("killdrop.toggle")) {
                pluginEnabled = !pluginEnabled;
                config.set("enabled", pluginEnabled);
                saveConfig();
                sender.sendMessage(ChatColor.GREEN + "PlayerKillDropPlugin is now " +
                        (pluginEnabled ? "enabled" : "disabled") + "!");
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        });

        // Command to reload the plugin's configuration
        getCommand("reloadkilldrop").setExecutor((sender, command, label, args) -> {
            if (sender.hasPermission("killdrop.reload")) {
                reloadConfig();
                config = getConfig();
                pluginEnabled = config.getBoolean("enabled", true);
                enabledWorlds = config.getStringList("enabledWorlds");
                sender.sendMessage(ChatColor.GREEN + "PlayerKillDropPlugin configuration reloaded.");
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        });
    }
}
