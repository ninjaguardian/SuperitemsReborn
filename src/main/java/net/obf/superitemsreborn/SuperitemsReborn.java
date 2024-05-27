package net.obf.superitemsreborn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SuperitemsReborn extends JavaPlugin implements Listener {

    private static final String VERSION_URL = "https://example.com/myplugin/version.json";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void checkForUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet request = new HttpGet(VERSION_URL);
                    try (CloseableHttpResponse response = httpClient.execute(request)) {
                        int statusCode = response.getCode();
                        if (statusCode == 200) {
                            String responseBody = EntityUtils.toString(response.getEntity());
                            getLogger().info("Response Body: " + responseBody); // Log the response body for debugging

                            try {
                                // Use Java's built-in JSON support to parse the JSON response
                                JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                                String latestVersion = json.get("version").getAsString();

                                if (isNewerVersion(latestVersion, getDescription().getVersion())) {
                                    getLogger().info("A new version of the plugin is available: " + latestVersion);
                                    getLogger().info("Please update to the latest version.");
                                } else {
                                    getLogger().info("You are running the latest version of the plugin.");
                                }
                            } catch (JsonSyntaxException e) {
                                getLogger().severe("Failed to parse JSON response: " + e.getMessage());
                            }
                        } else {
                            getLogger().warning("Failed to check for updates. HTTP Response Code: " + statusCode);
                        }
                    }
                } catch (Exception e) {
                    getLogger().severe("Error occurred while checking for updates: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }


    private boolean isNewerVersion(String latest, String current) {
        return latest.compareTo(current) > 0;
    }

    @Override
    public void onDisable() {
        getComponentLogger().debug(Component.text("Disabling Superitems Reborn"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.text("Hello, " + event.getPlayer().getName() + "!"));
    }

}