package com.yori3o.boss_checklist.common.client.data;


import com.yori3o.boss_checklist.common.BossChecklist;
import com.yori3o.boss_checklist.common.client.boss.BossDefinition;
import com.yori3o.boss_checklist.common.server.data.ServerBossIdsLoader;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.impl.PlatformUtil;

import net.minecraft.client.resources.language.ClientLanguage;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


// This class is not entirely client-side; server_bosses_ids.json is also loaded on the server
public class OverrideManager {


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Object FILE_IO_LOCK = new Object();

    public static final Path OVERRIDE_FOLDER = BossChecklist.CONFIG_FOLDER.resolve("override");

    public static final Path OVERRIDE_BOSSES_PATH = OVERRIDE_FOLDER.resolve("bosses.json");
    public static final Path OVERRIDE_SERVER_BOSSES_IDS_PATH = OVERRIDE_FOLDER.resolve("server_bosses_ids.json");
    public static final Path OVERRIDE_EN_US_PATH = OVERRIDE_FOLDER.resolve("en_us.json");

    public static final Map<String, BossDefinition> OVERRIDE_DEFINITIONS = new HashMap<>();
    public static final List<String> OVERRIDE_SERVER_BOSSES_IDS = new ArrayList<>();
    public static final Map<String, String> OVERRIDE_EN_US = new HashMap<>();


    public static final Path OVERRIDE_POSITIONS_PATH = BossChecklist.CONFIG_FOLDER.resolve("position_override.json");
    public static final Map<String, Float> OVERRIDE_POSITIONS = new HashMap<>();




    public static void saveAndAdd(BossDefinition boss, String name, String modName, String spawnInfo, String description, String additionalInfo) {
        loadOverrides();

        try {
            Files.createDirectories(OVERRIDE_FOLDER);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to create folder" + OVERRIDE_FOLDER.getFileName() + ": ", e);
        }

        List<BossDefinition> bosses = new ArrayList<>();
        bosses.addAll(OVERRIDE_DEFINITIONS.values());
        String id = boss.id();
        if (!OVERRIDE_DEFINITIONS.containsKey(id)) {
            bosses.add(boss);
            saveBossesOverride(bosses);
        }

        List<String> bossesServerIds = new ArrayList<>();
        bossesServerIds.addAll(OVERRIDE_SERVER_BOSSES_IDS);
        if (!OVERRIDE_SERVER_BOSSES_IDS.contains(id)) {
            bossesServerIds.add(id);
            ServerBossIdsLoader.LOADED_BOSSES.add(id);
            saveServerBossesIdsOverride(bossesServerIds);
        }

        Map<String, String> en_us = new HashMap<>(OVERRIDE_EN_US);
        boolean translationsChanged = false;
        String key = "boss_checklist.boss." + id.replace(":", "_");
        if (!OVERRIDE_EN_US.containsKey(key)) {
            en_us.put(key, name);
            OVERRIDE_EN_US.put(key, name);
            translationsChanged = true;
        }
        String keyMod = "boss_checklist.mod." + boss.modId();
        if (!OVERRIDE_EN_US.containsKey(keyMod) && !ClientLanguage.getInstance().has(keyMod)) {
            en_us.put(keyMod, modName);
            OVERRIDE_EN_US.put(keyMod, modName);
            translationsChanged = true;
        }
        String keySummon = "boss_checklist.summon." + id.replace(":", "_");
        if (!OVERRIDE_EN_US.containsKey(keySummon)) {
            en_us.put(keySummon, spawnInfo);
            OVERRIDE_EN_US.put(keySummon, spawnInfo);
            translationsChanged = true;
        }
        String keyDescription = "boss_checklist.desc." + id.replace(":", "_");
        if (!OVERRIDE_EN_US.containsKey(keyDescription) && description != null && !description.isEmpty()) {
            en_us.put(keyDescription, description);
            OVERRIDE_EN_US.put(keyDescription, description);
            translationsChanged = true;
        }
        String keyAddtl = "boss_checklist.info." + id.replace(":", "_");
        if (!OVERRIDE_EN_US.containsKey(keyAddtl) && additionalInfo != null && !additionalInfo.isEmpty()) {
            en_us.put(keyAddtl, additionalInfo);
            OVERRIDE_EN_US.put(keyAddtl, additionalInfo);
            translationsChanged = true;
        }
        if (translationsChanged) {
            saveEnUsOverride(en_us);
        }
    }

    public static boolean saveEditedBoss(BossDefinition boss, String name, String modTranslationKey, String modName, String spawnInfo, String description, String additionalInfo) {
        loadOverrides();

        try {
            Files.createDirectories(OVERRIDE_FOLDER);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to create folder" + OVERRIDE_FOLDER.getFileName() + ": ", e);
            return false;
        }

        List<BossDefinition> bosses = new ArrayList<>(OVERRIDE_DEFINITIONS.values());
        bosses.removeIf(existing -> existing.id().equals(boss.id()));
        bosses.add(boss);
        String bossTranslationId = boss.id().replace(":", "_");
        Map<String, String> translations = new HashMap<>(OVERRIDE_EN_US);
        translations.put("boss_checklist.boss." + bossTranslationId, name);
        translations.put(modTranslationKey, modName);
        translations.put("boss_checklist.summon." + bossTranslationId, spawnInfo);
        translations.put("boss_checklist.desc." + bossTranslationId, description);
        translations.put("boss_checklist.info." + bossTranslationId, additionalInfo);

        if (!saveBossesOverride(bosses) || !saveEnUsOverride(translations)) {
            return false;
        }

        OVERRIDE_DEFINITIONS.put(boss.id(), boss);
        OVERRIDE_EN_US.clear();
        OVERRIDE_EN_US.putAll(translations);
        return true;
    }

    /// ------------ LOADER ------------
    public static void loadOverrides() {
        OVERRIDE_DEFINITIONS.clear();
        OVERRIDE_EN_US.clear();

        loadPositionOverride();

        loadServerOverride();

        if (PlatformUtil.isClient()) {
            if (OVERRIDE_BOSSES_PATH.toFile().exists()) {
                try (Reader reader = Files.newBufferedReader(OVERRIDE_BOSSES_PATH)) {
                    Type listType = new TypeToken<List<BossDefinition>>() {}.getType();
                    List<BossDefinition> list = GSON.fromJson(new JsonReader(reader), listType);

                    for (BossDefinition def : list) {
                        OVERRIDE_DEFINITIONS.put(def.id(), def);
                    }
                } catch (Exception e) {
                    LoggerUtil.errorWithException("Failed to load " + OVERRIDE_BOSSES_PATH.getFileName() + ": ", e);
                }
            }

            if (OVERRIDE_EN_US_PATH.toFile().exists()) {
                try (Reader reader = Files.newBufferedReader(OVERRIDE_EN_US_PATH)) {
                    Type listType = new TypeToken<Map<String, String>>() {}.getType();
                    Map<String, String> list = GSON.fromJson(new JsonReader(reader), listType);
                    OVERRIDE_EN_US.putAll(list);
                } catch (Exception e) {
                    LoggerUtil.errorWithException("Failed to load " + OVERRIDE_EN_US_PATH.getFileName() + ": ", e);
                }
            }
        }
    }

    public static void loadServerOverride() {
        OVERRIDE_SERVER_BOSSES_IDS.clear();
        if (!OVERRIDE_SERVER_BOSSES_IDS_PATH.toFile().exists()) return;
        try (Reader reader = Files.newBufferedReader(OVERRIDE_SERVER_BOSSES_IDS_PATH)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> list = GSON.fromJson(new JsonReader(reader), listType);
            OVERRIDE_SERVER_BOSSES_IDS.addAll(list);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to load " + OVERRIDE_SERVER_BOSSES_IDS_PATH.getFileName() + ": ", e);
        }
    }

    public static void loadPositionOverride() {
        OVERRIDE_POSITIONS.clear();
        if (!OVERRIDE_POSITIONS_PATH.toFile().exists()) return;
        try (Reader reader = Files.newBufferedReader(OVERRIDE_POSITIONS_PATH)) {
            Type mapType = new TypeToken<Map<String, Float>>() {}.getType();
            Map<String, Float> map = GSON.fromJson(new JsonReader(reader), mapType);
            OVERRIDE_POSITIONS.putAll(map);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to load " + OVERRIDE_POSITIONS_PATH.getFileName() + ": ", e);
        }
    }

    public static void addPositionOverride(String bossId, float position) {
        if (OVERRIDE_POSITIONS.containsKey(bossId)) {
            OVERRIDE_POSITIONS.remove(bossId);
        }
        OVERRIDE_POSITIONS.put(bossId, position);
    }

    ///===========================
    /// SAVERS
    /// ===========================
     
    public static boolean saveBossesOverride(List<BossDefinition> list) {
        try (Writer writer = Files.newBufferedWriter(OVERRIDE_BOSSES_PATH)) {
            GSON.toJson(list, writer);
            return true;
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to save " + OVERRIDE_BOSSES_PATH.getFileName() + ": ", e);
            return false;
        }
    }
    public static void saveServerBossesIdsOverride(List<String> list) {
        try (Writer writer = Files.newBufferedWriter(OVERRIDE_SERVER_BOSSES_IDS_PATH)) {
            GSON.toJson(list, writer);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to save " + OVERRIDE_SERVER_BOSSES_IDS_PATH.getFileName() + ": ", e);
        }
    }
    public static boolean saveEnUsOverride(Map<String, String> map) {
        try (Writer writer = Files.newBufferedWriter(OVERRIDE_EN_US_PATH)) {
            GSON.toJson(map, writer);
            return true;
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to save " + OVERRIDE_EN_US_PATH.getFileName() + ": ", e);
            return false;
        }
    }
    
    public static void savePositionOverride(Map<String, Float> map) {
        CompletableFuture.runAsync(() -> {
            synchronized (FILE_IO_LOCK) {
                try (Writer writer = Files.newBufferedWriter(OVERRIDE_POSITIONS_PATH)) {
                    GSON.toJson(map, writer);
                } catch (Exception e) {
                    LoggerUtil.errorWithException("Failed to save " + OVERRIDE_POSITIONS_PATH.getFileName() + ": ", e);
                }
            }
        });
    }
}
