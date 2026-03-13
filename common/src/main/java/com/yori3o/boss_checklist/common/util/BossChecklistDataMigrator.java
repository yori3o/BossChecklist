package com.yori3o.boss_checklist.common.util;


import com.yori3o.boss_checklist.common.client.data.ClientDataSaver;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;


/**
 * In 4.0.0 the client save folder was renamed, so the data needs to be transferred.
 */
public class BossChecklistDataMigrator {


    private static final String OLD_FOLDER = "bossСhecklist_data";
    private static final String NEW_FOLDER = ClientDataSaver.DATA_FOLDER_NAME;


    public static void migrateIfNeeded() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        
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
            LoggerUtil.LOGGER.error("Failed to migrate BossChecklist data folder!", e);
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
            java.nio.file.Files.copy(
                    source.toPath(),
                    target.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
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
