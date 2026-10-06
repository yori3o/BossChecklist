package com.yori3o.boss_checklist.common.util;


import com.yori3o.boss_checklist.common.BossChecklist;
import com.yori3o.boss_checklist.common.client.data.ClientDataSaver;
import com.yori3o.boss_checklist.common.client.data.OverrideManager;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.yori3o.boss_checklist.impl.PlatformUtil;


/**
 * Migrates user data when its folder or file names change.
 */
public class BossChecklistDataMigrator {


    private static final String OLD_FOLDER = "bossСhecklist_data";
    private static final String NEW_FOLDER = ClientDataSaver.DATA_FOLDER_NAME;
    private static final Path OLD_CLIENT_CONFIG = PlatformUtil.getConfigDir().resolve("boss_checklist_client.json");
    private static final Path NEW_CLIENT_CONFIG = BossChecklist.CONFIG_FOLDER.resolve("boss_checklist-client.json");
    private static final Path OLD_SERVER_CONFIG = PlatformUtil.getConfigDir().resolve("boss_checklist_server.json");
    private static final Path NEW_SERVER_CONFIG = BossChecklist.CONFIG_FOLDER.resolve("boss_checklist-server.json");
    private static final Path OLD_OVERRIDE_FOLDER = BossChecklist.CONFIG_FOLDER.resolve("overlap");
    private static final Path OLD_OVERRIDE_POSITIONS = BossChecklist.CONFIG_FOLDER.resolve("position_overlap.json");


    public static void migrateConfigFiles() {
        try {
            migrateFile(OLD_CLIENT_CONFIG, NEW_CLIENT_CONFIG);
            migrateFile(OLD_SERVER_CONFIG, NEW_SERVER_CONFIG);
        } catch (IOException e) {
            LoggerUtil.errorWithException("Failed to migrate legacy config files: ", e);
        }
    }

    public static void migrateIfNeeded() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            LoggerUtil.error("Failed to migrate data due to Minecraft.getInstance() is null.");
            return;
        }
        File gameDir = mc.gameDirectory;

        File oldDir = new File(gameDir, OLD_FOLDER);
        File newDir = new File(gameDir, NEW_FOLDER);

        if (!oldDir.exists()) return;

        if (!newDir.exists() && !newDir.mkdirs()) {
            LoggerUtil.error("Failed to create new data folder: " + newDir.getAbsolutePath());
            return;
        }

        try {
            copyFolder(oldDir, newDir);
            deleteFolder(oldDir);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to migrate BossChecklist data folder!", e);
        }
    }

    public static void migrateOverrideFiles() {
        try {
            migrateDirectory(OLD_OVERRIDE_FOLDER, OverrideManager.OVERRIDE_FOLDER);
            migrateFile(OLD_OVERRIDE_POSITIONS, OverrideManager.OVERRIDE_POSITIONS_PATH);
        } catch (IOException e) {
            LoggerUtil.errorWithException("Failed to migrate legacy override files: ", e);
        }
    }

    private static void migrateDirectory(Path source, Path target) throws IOException {
        if (!Files.isDirectory(source)) return;

        Files.createDirectories(target);
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(source)) {
            for (Path entry : entries) {
                Path targetEntry = target.resolve(entry.getFileName());
                if (Files.isDirectory(entry)) {
                    migrateDirectory(entry, targetEntry);
                    if (isDirectoryEmpty(entry)) {
                        Files.delete(entry);
                    }
                } else {
                    migrateFile(entry, targetEntry);
                }
            }
        }

        if (isDirectoryEmpty(source)) {
            Files.delete(source);
        }
    }

    private static void migrateFile(Path source, Path target) throws IOException {
        if (!Files.exists(source)) return;
        if (Files.exists(target)) {
            LoggerUtil.warn("Keeping legacy file because the new file already exists: " + source);
            return;
        }

        Files.createDirectories(target.getParent());
        Files.move(source, target);
    }

    private static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
            return !entries.iterator().hasNext();
        }
    }

    private static void copyFolder(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists() && !target.mkdirs()) {
                throw new IOException("Failed to create directory: " + target.getAbsolutePath());
            }

            File[] files = source.listFiles();
            if (files == null) return;

            for (File file : files) {
                copyFolder(file, new File(target, file.getName()));
            }
        } else {
            Files.copy(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private static void deleteFolder(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteFolder(file);
            }
        }

        if (!dir.delete()) {
            throw new IOException("Failed to delete: " + dir.getAbsolutePath());
        }
    }
}
