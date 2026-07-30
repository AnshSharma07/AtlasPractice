package com.ansh.atlaspractice.update;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener implements Listener {

    private final AtlasPracticePlugin plugin;

    public UpdateListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        if (!e.getPlayer().isOp())
            return;

        VersionChecker checker = plugin.getVersionChecker();

        if (checker == null)
            return;

        if (!checker.isUpdateAvailable()) {

            e.getPlayer().sendMessage(ChatColor.GREEN +
                    "AtlasPractice is running the latest version.");

            return;

        }

        e.getPlayer().sendMessage("");

        e.getPlayer().sendMessage(ChatColor.RED + "AtlasPractice Update Available");

        e.getPlayer().sendMessage(ChatColor.GRAY +
                "Current Version: " +
                plugin.getDescription().getVersion());

        e.getPlayer().sendMessage(ChatColor.GRAY +
                "Latest Version: " +
                checker.getLatestVersion());

        e.getPlayer().sendMessage(ChatColor.YELLOW +
                "Download:");

        e.getPlayer().sendMessage(ChatColor.AQUA +
                "https://modularboyansh.xyz/updates");

        e.getPlayer().sendMessage("");

    }

}