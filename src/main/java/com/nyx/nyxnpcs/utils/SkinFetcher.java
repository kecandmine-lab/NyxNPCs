package com.nyx.nyxnpcs.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class SkinFetcher {

    private static final String MOJANG_API_UUID = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MOJANG_API_PROFILE = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static void fetchSkin(String playerName, BiConsumer<String, String> callback) {
        CompletableFuture.runAsync(() -> {
            try {
                String uuid = getUUID(playerName);
                if (uuid == null) {
                    Bukkit.getScheduler().runTask(
                        Bukkit.getPluginManager().getPlugin("NyxNPCs"),
                        () -> callback.accept(null, null)
                    );
                    return;
                }

                String[] skinData = getSkinData(uuid);
                Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager().getPlugin("NyxNPCs"),
                    () -> callback.accept(skinData[0], skinData[1])
                );

            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager().getPlugin("NyxNPCs"),
                    () -> callback.accept(null, null)
                );
            }
        });
    }

    private static String getUUID(String playerName) {
        try {
            URL url = new URI(MOJANG_API_UUID + playerName).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                return null;
            }

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();

            return json.get("id").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String[] getSkinData(String uuid) {
        try {
            URL url = new URI(MOJANG_API_PROFILE + uuid + "?unsigned=false").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                return new String[]{null, null};
            }

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();

            JsonObject textures = json.getAsJsonArray("properties").get(0).getAsJsonObject();
            String value = textures.get("value").getAsString();
            String signature = textures.get("signature").getAsString();

            return new String[]{value, signature};

        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{null, null};
        }
    }
}
