package com.ansh.atlaspractice.update;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.Bukkit;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class VersionChecker {

    private static final String VERSION_URL =
            "https://modularboyansh.xyz/version.txt";

    private final AtlasPracticePlugin plugin;

    private boolean updateAvailable = false;
    private String latestVersion = "";

    public VersionChecker(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void check() {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {

                URL url = new URL(VERSION_URL);

                HttpsURLConnection connection =
                        (HttpsURLConnection) url.openConnection();

                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(connection.getInputStream()));

                latestVersion = reader.readLine().trim();

                reader.close();

                String current = plugin.getDescription().getVersion();

                updateAvailable = !current.equalsIgnoreCase(latestVersion);

                Bukkit.getScheduler().runTask(plugin, () -> {

                    if (updateAvailable) {

                        plugin.getLogger().warning("--------------------------------------------");
                        plugin.getLogger().warning("A new AtlasPractice update is available!");
                        plugin.getLogger().warning("Current : " + current);
                        plugin.getLogger().warning("Latest  : " + latestVersion);
                        plugin.getLogger().warning("Download:");
                        plugin.getLogger().warning("https://modularboyansh.xyz/updates");
                        plugin.getLogger().warning("--------------------------------------------");

                    } else {

                        plugin.getLogger().info("AtlasPractice is running the latest version.");

                    }

                });

            } catch (Exception ignored) {

            }

        });

    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

}