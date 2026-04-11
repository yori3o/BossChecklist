package com.yori3o.boss_checklist.common;


import java.nio.file.Files;
import java.nio.file.Path;

import com.yori3o.boss_checklist.common.client.data.OverlapManager;
import com.yori3o.boss_checklist.common.command.SetDefeatedCommand;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.impl.PlatformUtil;



public class BossChecklist {

    
    public static final String MOD_ID = "boss_checklist";

    public static final Path CONFIG_FOLDER = PlatformUtil.getConfigDir().resolve("boss_checklist");


    public void init() {

        try {
            Files.createDirectories(OverlapManager.OVERLAP_FOLDER);
        } catch (Exception e) {
            LoggerUtil.errorWithException("Failed to create folder" + OverlapManager.OVERLAP_FOLDER.getFileName() + ": ", e);
        }
        OverlapManager.loadOverlaps();

        SetDefeatedCommand.register();

        if (PlatformUtil.isClient()) {
            BossChecklistClient.initClient();
        }
    }
}