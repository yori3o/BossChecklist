package com.yori3o.boss_checklist.common.client.data;


import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.impl.PlatformUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.level.storage.LevelResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * This class is used to work with client-side saving of marked bosses.
 */
public class ClientDataSaver {


    private static final Gson GSON = new GsonBuilder().create();

    public static final String DATA_FOLDER_NAME = ".boss_checklist_data";
    public static final String JSON_FILE_NAME = "defeated_bosses.json";

    private static String worldKey;

    private static final Set<String> defeatedIds = new LinkedHashSet<>();


    public static int defeatedBossesCount() {
        return defeatedIds.size();
    }


    public static boolean isDefeated(String bossId) {
        return defeatedIds.contains(bossId);
    }


    public static void load() {
        updateWorldKeyIfNeeded();

        defeatedIds.clear();

        File file = getFile();
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> list = GSON.fromJson(reader, type);

            if (list != null) {
                for (String id : list) {
                    if (PlatformUtil.isModLoaded(id.split(":")[0])) {
                        defeatedIds.add(id);
                    }
                }
            }

        } catch (Exception e) {
            LoggerUtil.LOGGER.error("Failed to load defeated bosses file", e);
        }

        for (String id : defeatedIds) {
            BossService.get(id).progress().markDefeatedClient(true);
        }
    }


    public static void setDefeated(String bossId, boolean defeated) {
        updateWorldKeyIfNeeded();

        boolean changed;

        if (defeated) {
            changed = defeatedIds.add(bossId);
        } else {
            changed = defeatedIds.remove(bossId);
        }

        if (!changed) return;

        save();
    }


    private static void save() {
        File file = getFile();

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(defeatedIds, writer);
        } catch (Exception e) {
            LoggerUtil.LOGGER.error("Failed to save defeated bosses file", e);
        }
    }


    private static File getFile() {
        File folder = new File(Minecraft.getInstance().gameDirectory,
                DATA_FOLDER_NAME + File.separator + worldKey);

        if (!folder.exists()) folder.mkdirs();

        return new File(folder, JSON_FILE_NAME);
    }


    private static void updateWorldKeyIfNeeded() {
        if (worldKey == null) updateWorldKey();
    }

    
    public static void updateWorldKey() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.isLocalServer()) {
            worldKey = "singleplayer_" + mc.getSingleplayerServer()
                    .getWorldPath(LevelResource.ROOT)
                    .normalize()
                    .getFileName()
                    .toString();
            return;
        }

        ClientPacketListener connection = mc.getConnection();
        if (connection != null && connection.getServerData() != null) {
            worldKey = "multiplayer_" + connection.getServerData().ip.replace(":", "_");
            return;
        }

        worldKey = "INVALID_WORLD_KEY";
    }
}
