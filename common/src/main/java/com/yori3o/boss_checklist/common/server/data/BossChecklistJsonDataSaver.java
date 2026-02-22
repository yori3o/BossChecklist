package com.yori3o.boss_checklist.common.server.data;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * This class saves and reads data from the world folder.
 * The JSON format provides more freedom, readability, and is independent of the Minecraft version.
 */
public class BossChecklistJsonDataSaver {


    public static final Object FILE_IO_LOCK = new Object();

    private static final Gson GSON = new GsonBuilder().create();

    private static final String FOLDER_NAME = "boss_checklist";

    private static final String FILE_GLOBAL_STATISTICS = "global_statistics.json";
    private static final String FILE_LATEST_BOSS_BATTLES = "latest_boss_battles.json";
    private static final String FILE_DEFEATED_BOSSES = "defeated_bosses.json";


    // ---------------------------
    // Public API
    // ---------------------------

    public static void saveGlobalStatistics(File worldDir, Map<String, Float> playerDamages) throws IOException {
        File file = new File(getFolder(worldDir), FILE_GLOBAL_STATISTICS);
        saveMap(file, playerDamages, new TypeToken<Map<String, Float>>() {}.getType());
    }

    public static Map<String, Float> loadGlobalStatistics(File worldDir) throws IOException {
        File file = new File(getFolder(worldDir), FILE_GLOBAL_STATISTICS);
        return loadMap(file, new TypeToken<Map<String, Float>>() {}.getType());
    }


    public static void saveLatestBossBattles(File worldDir, Map<String, ServerBossAttempt> serverBossAttempts) throws IOException {
        File file = new File(getFolder(worldDir), FILE_LATEST_BOSS_BATTLES);
        saveMap(file, serverBossAttempts, new TypeToken<Map<String, ServerBossAttempt>>() {}.getType());
    }

    public static Map<String, ServerBossAttempt> loadLatestBossBattles(File worldDir) throws IOException {
        File file = new File(getFolder(worldDir), FILE_LATEST_BOSS_BATTLES);
        return loadMap(file, new TypeToken<Map<String, ServerBossAttempt>>() {}.getType());
    }


    public static void saveDefeatedBosses(File worldDir, Map<String, String> bossKillers) throws IOException {
        File file = new File(getFolder(worldDir), FILE_DEFEATED_BOSSES);
        saveMap(file, bossKillers, new TypeToken<Map<String, String>>() {}.getType());
    }

    public static Map<String, String> loadDefeatedBosses(File worldDir) throws IOException {
        File file = new File(getFolder(worldDir), FILE_DEFEATED_BOSSES);
        return loadMap(file, new TypeToken<Map<String, String>>() {}.getType());
    }


    // ---------------------------
    // Internal helpers
    // ---------------------------

    private static File getFolder(File worldDir) throws IOException {
        File folder = new File(worldDir, FOLDER_NAME);

        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("Failed to create boss checklist folder: " + folder.getAbsolutePath());
        }

        return folder;
    }

    private static <T> void saveMap(File file, T map, Type type) throws IOException {
        synchronized (FILE_IO_LOCK) {
            // if null, we save an empty structure
            if (map == null) {
                map = GSON.fromJson("{}", type);
            }

            String json = GSON.toJson(map, type);

            // atomic write: file.tmp -> file
            File tmp = new File(file.getParentFile(), file.getName() + ".tmp");

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmp), java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(json);
            }

            java.nio.file.Files.move(
                    tmp.toPath(),
                    file.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE
            );
        }
    }

    private static <T> T loadMap(File file, Type type) throws IOException {
        if (!file.exists()) {
            // if the file does not exist, we return an empty map
            return GSON.fromJson("{}", type);
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            T loaded = GSON.fromJson(reader, type);

            // if the file is empty/broken/returned null
            if (loaded == null) {
                return GSON.fromJson("{}", type);
            }

            return loaded;
        }
    }
}
