package net.obf.superitemsreborn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SuperitemsReborn extends JavaPlugin implements Listener {

    private static final URL VERSION_URL;

    static {
        try {
            VERSION_URL = new URL("https://raw.githubusercontent.com/ninjaguardian/SuperitemsReborn/master/updates.json");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL format", e);
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        checkForUpdates();
    }

    private void checkForUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    // Use VERSION_URL to fetch updates.json
                    connection = (HttpURLConnection) VERSION_URL.openConnection();
                    connection.setRequestMethod("GET");

                    int statusCode = connection.getResponseCode();
                    if (statusCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine())!= null) {
                            response.append(line);
                        }
                        reader.close();

                        String responseBody = response.toString();
                        getComponentLogger().info("Response Body: {}", responseBody);

                        // Parse the JSON response
                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        String currentMinecraftVersion = getDescription().getVersion().split("-")[1]; // Extract Minecraft version
                        String currentPluginVersion = getDescription().getVersion().split("-")[0]; // Extract Plugin version

                        // Find the latest version for the current Minecraft version
                        JsonObject latestVersionObj = null;
                        for (String element : json.keySet()) {
                            if (element.equals(currentMinecraftVersion)) {
                                latestVersionObj = json.get(element).getAsJsonObject();
                                break;
                            }
                        }

                        if (latestVersionObj!= null && isNewerVersion(latestVersionObj.get("version").getAsString(), currentPluginVersion)) {
                            getComponentLogger().warn("A new version of the plugin is available: {}", latestVersionObj.get("version").getAsString());
                            getComponentLogger().warn("Please update to the latest version.");
                        } else {
                            getComponentLogger().info("You are running the latest version of the plugin.");
                        }
                    } else {
                        getComponentLogger().error("Failed to check for updates. HTTP Response Code: {}", statusCode);
                    }
                } catch (IOException e) {
                    getComponentLogger().error("Error occurred while checking for updates: {}", e.getMessage());
                } finally {
                    if (connection!= null) {
                        try {
                            connection.disconnect();
                        } catch (Exception e) {
                            getComponentLogger().error("Failed to disconnect HttpURLConnection", e);
                        }
                    }
                }
            }
        }.runTaskAsynchronously(this);
    }



    private boolean isNewerVersion(String latest, String current) {
        String[] latestComponents = latest.split("\\.");
        String[] currentComponents = current.split("\\.");

        if (latestComponents.length!= currentComponents.length) {
            throw new IllegalArgumentException("Versions must have the same number of components");
        }

        for (int i = 0; i < latestComponents.length; i++) {
            int latestComponent = Integer.parseInt(latestComponents[i]);
            int currentComponent = Integer.parseInt(currentComponents[i]);

            getComponentLogger().warn("latestComponent: {}",latestComponent);
            getComponentLogger().warn("currentComponent: {}",currentComponent);
            if (currentComponent > latestComponent) {
                return false;
            } else if (currentComponent < latestComponent) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDisable() {
        getComponentLogger().info(Component.text("Disabling SuperitemsReborn"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.text("Hello, " + event.getPlayer().getName() + "!"));
    }
}
