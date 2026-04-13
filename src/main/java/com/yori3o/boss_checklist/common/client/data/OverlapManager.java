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



public class OverlapManager {


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final Path OVERLAP_FOLDER = BossChecklist.CONFIG_FOLDER.resolve("overlap");

    public static final Path OVERLAP_BOSSES_PATH = OVERLAP_FOLDER.resolve("bosses.json");
    public static final Path OVERLAP_SERVER_BOSSES_IDS_PATH = OVERLAP_FOLDER.resolve("server_bosses_ids.json");
    public static final Path OVERLAP_EN_US_PATH = OVERLAP_FOLDER.resolve("en_us.json");

    public static final Map<String, BossDefinition> OVERLAP_DEFINITIONS = new HashMap<>();
    public static final List<String> OVERLAP_SERVER_BOSSES_IDS = new ArrayList<>();
    public static final Map<String, String> OVERLAP_EN_US = new HashMap<>();


    public static void saveOrAdd(BossDefinition boss, String name, String modName, String spawnInfo) {
        loadOverlaps();

        List<BossDefinition> bosses = new ArrayList<>();
        bosses.addAll(OVERLAP_DEFINITIONS.values());
        String id = boss.id();
        if (!OVERLAP_DEFINITIONS.containsKey(id)) {
            bosses.add(boss);
            saveBossesOverlap(bosses);
        }

        List<String> bossesServerIds = new ArrayList<>();
        bossesServerIds.addAll(OVERLAP_SERVER_BOSSES_IDS);
        if (!OVERLAP_SERVER_BOSSES_IDS.contains(id)) {
            bossesServerIds.add(id);
            ServerBossIdsLoader.LOADED_BOSSES.add(id);
            saveServerBossesIdsOverlap(bossesServerIds);
        }

        Map<String, String> en_us = new HashMap<>();
        en_us.putAll(OVERLAP_EN_US);
        String key = "boss_checklist.boss." + id.replace(":", "_");
        if (!OVERLAP_EN_US.containsKey(key)) {
            en_us.put(key, name);
            OVERLAP_EN_US.put(key, name);
        }
        String keyMod = "boss_checklist.mod." + boss.modId();
        if (!OVERLAP_EN_US.containsKey(keyMod) && !ClientLanguage.getInstance().has(keyMod)) {
            en_us.put(keyMod, modName);
            OVERLAP_EN_US.put(keyMod, modName);
        }
        String keySummon = "boss_checklist.summon." + id.replace(":", "_");
        if (!OVERLAP_EN_US.containsKey(keySummon)) {
            en_us.put(keySummon, spawnInfo);
            OVERLAP_EN_US.put(keySummon, spawnInfo);
            saveEnUsOverlap(en_us);
        }
    }

    /// ------------ LOADER ------------
    public static void loadOverlaps() {
        OVERLAP_DEFINITIONS.clear();
        OVERLAP_SERVER_BOSSES_IDS.clear();
        OVERLAP_EN_US.clear();

        if (!OVERLAP_BOSSES_PATH.toFile().exists()) return;

        loadServerOverlap();

        if (PlatformUtil.isClient()) {
            try (Reader reader = Files.newBufferedReader(OVERLAP_BOSSES_PATH)) {
                Type listType = new TypeToken<List<BossDefinition>>() {}.getType();
                List<BossDefinition> list = GSON.fromJson(new JsonReader(reader), listType);

                for (BossDefinition def : list) {
                    OVERLAP_DEFINITIONS.put(def.id(), def);
                }
            } catch (Exception e) {
                LoggerUtil.errorWithException("Failed to load " + OVERLAP_BOSSES_PATH.getFileName() + ": ", e);
            }


            try (Reader reader = Files.newBufferedReader(OVERLAP_EN_US_PATH)) {
                Type listType = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> list = GSON.fromJson(new JsonReader(reader), listType);
                OVERLAP_EN_US.putAll(list);
            } catch (Exception e) {
                LoggerUtil.errorWithException("Failed to load " + OVERLAP_EN_US_PATH.getFileName() + ": ", e);
            }
        }
    }

    public static void loadServerOverlap() {
        if (!OVERLAP_SERVER_BOSSES_IDS_PATH.toFile().exists()) return;
        try (Reader reader = Files.newBufferedReader(OVERLAP_SERVER_BOSSES_IDS_PATH)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> list = GSON.fromJson(new JsonReader(reader), listType);
            OVERLAP_SERVER_BOSSES_IDS.addAll(list);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to load " + OVERLAP_SERVER_BOSSES_IDS_PATH.getFileName() + ": ", e);
        }
    }

    ///===========================
    /// SAVERS
    /// ===========================
     
    public static void saveBossesOverlap(List<BossDefinition> list) {
        try (Writer writer = Files.newBufferedWriter(OVERLAP_BOSSES_PATH)) {
            GSON.toJson(list, writer);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to save " + OVERLAP_BOSSES_PATH.getFileName() + ": ", e);
        }
    }
    public static void saveServerBossesIdsOverlap(List<String> list) {
        try (Writer writer = Files.newBufferedWriter(OVERLAP_SERVER_BOSSES_IDS_PATH)) {
            GSON.toJson(list, writer);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to save " + OVERLAP_SERVER_BOSSES_IDS_PATH.getFileName() + ": ", e);
        }
    }
    public static void saveEnUsOverlap(Map<String, String> map) {
        try (Writer writer = Files.newBufferedWriter(OVERLAP_EN_US_PATH)) {
            GSON.toJson(map, writer);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to save " + OVERLAP_EN_US_PATH.getFileName() + ": ", e);
        }
    }
}
