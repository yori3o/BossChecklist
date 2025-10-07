package com.yori3o.boss_checklist.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.level.storage.LevelResource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BossDataClientSaver {

    private static final Logger LOGGER = LogManager.getLogger("boss_checklist");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FOLDER_NAME = "bossСhecklist_data";
    private static final String JSON_FILE_NAME = "defeated_bosses.json";

    public static List<String> defeatedBosses_InWorld = new ArrayList<>();
    public static List<String> defeatedBossesIds_InWorld = new ArrayList<>();

    private static String worldKey;


    public static void BossDefeated(String bossId, Boolean bool) {

        File dataFolder = new File(Minecraft.getInstance().gameDirectory, DATA_FOLDER_NAME + File.separator + worldKey);
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            LOGGER.error("Failed to create data folder for world key: " + worldKey);
            return;
        }

        File jsonFile = new File(dataFolder, JSON_FILE_NAME);
        List<String> defeatedBosses = new ArrayList<>();

        if (jsonFile.exists()) {
            try (FileReader reader = new FileReader(jsonFile)) {
                Type listType = new TypeToken<List<String>>() {}.getType();
                defeatedBosses = GSON.fromJson(reader, listType);
                if (defeatedBosses == null) {
                    defeatedBosses = new ArrayList<>();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to read JSON file from " + jsonFile.getAbsolutePath(), e);
            }
        }
        if (bool) {
            if (!defeatedBosses.contains(bossId)) {
                defeatedBosses.add(bossId);
                try (FileWriter writer = new FileWriter(jsonFile)) {
                    GSON.toJson(defeatedBosses, writer);
                } catch (IOException e) {
                    LOGGER.error("Failed to write to JSON file " + jsonFile.getAbsolutePath(), e);
                }
            }
        } else {
            if (defeatedBosses.contains(bossId)) {
                defeatedBosses.remove(bossId);
                try (FileWriter writer = new FileWriter(jsonFile)) {
                    GSON.toJson(defeatedBosses, writer);
                } catch (IOException e) {
                    LOGGER.error("Failed to write to JSON file " + jsonFile.getAbsolutePath(), e);
                }
            }
        }

    }


    public Boolean isBossDefeated(String bossId) {
        
        File jsonFile = new File(Minecraft.getInstance().gameDirectory, DATA_FOLDER_NAME + File.separator + worldKey + File.separator + JSON_FILE_NAME);

        if (!jsonFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> defeatedBosses = GSON.fromJson(reader, listType);
            if (defeatedBosses != null) {

                return defeatedBosses.contains(bossId);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read JSON file from " + jsonFile.getAbsolutePath(), e);
        }

        return false;
    }


    public static void UpdateCurrentWorldKey() {
        Minecraft minecraft = Minecraft.getInstance();

        // Check if the current world is a single-player instance.
        // This is the correct way to handle it on the client side.
        if (minecraft.isLocalServer()) {
            worldKey = "singleplayer_" + minecraft.getSingleplayerServer().getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
            return;
        }

        // If it's not a local server, check if it's a multiplayer connection.
        ClientPacketListener clientPacketListener = minecraft.getConnection();
        if (clientPacketListener != null) {
            ServerData serverData = clientPacketListener.getServerData();
            if (serverData != null) {
                worldKey = "multiplayer_" + serverData.ip.replace(":", "_");
                return;
            }
        }
        
        // if neither a local nor a multiplayer connection is found.
        worldKey = "INVALID_WORLD_KEY";
    }

    public int defeatedBossesCount() {
        
        File jsonFile = new File(Minecraft.getInstance().gameDirectory, DATA_FOLDER_NAME + File.separator + worldKey + File.separator + JSON_FILE_NAME);

        if (!jsonFile.exists()) {
            return 0;
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> defeatedBosses = GSON.fromJson(reader, listType);
            if (defeatedBosses != null) {
                return defeatedBosses.size();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read JSON file from " + jsonFile.getAbsolutePath(), e);
        }

        return 0;
    }


    // set defeated bosses checkmarks to true from server defeated bosses
    public static void SetDefeatedsWhenJoiningToServer() {

        for (String lineOfInformation : defeatedBosses_InWorld) {

            String bossId = lineOfInformation.split("#")[0];

            BossDefeated(bossId, true);
        }

    }

    public static String whoKiller(String bossId) {

        for (String lineOfInformation : defeatedBosses_InWorld) {

            String bossIdFromList = lineOfInformation.split("#")[0];

            if (bossId.equals(bossIdFromList)) {
                if (lineOfInformation.split("#").length == 1) {
                    return "";
                } else {
                    return lineOfInformation.split("#")[1];
                }

                
            }
            
        }
        return "ERROR";
    }
}