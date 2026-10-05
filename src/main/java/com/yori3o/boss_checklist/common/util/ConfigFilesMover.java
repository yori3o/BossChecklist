package com.yori3o.boss_checklist.common.util;


import java.nio.file.*;

import com.yori3o.boss_checklist.common.BossChecklist;
import com.yori3o.boss_checklist.impl.PlatformUtil;


public class ConfigFilesMover {


    private static final Path OLD_CLIENT = PlatformUtil.getConfigDir().resolve("boss_checklist_client.json");
    private static final Path NEW_CLIENT = BossChecklist.CONFIG_FOLDER.resolve("boss_checklist-client.json");
    
    private static final Path OLD_SERVER = PlatformUtil.getConfigDir().resolve("boss_checklist_server.json");
    private static final Path NEW_SERVER = BossChecklist.CONFIG_FOLDER.resolve("boss_checklist-server.json");

    
    public static void moveConfigFiles() {
        if (Files.exists(NEW_SERVER)) return;
        if (!Files.exists(OLD_SERVER)) return;
        try {
            Files.createDirectories(BossChecklist.CONFIG_FOLDER);
            if (Files.exists(NEW_CLIENT)) Files.move(OLD_CLIENT, NEW_CLIENT, StandardCopyOption.REPLACE_EXISTING);
            Files.move(OLD_SERVER, NEW_SERVER, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Error when moving config files: ", e);
        }
    }
}