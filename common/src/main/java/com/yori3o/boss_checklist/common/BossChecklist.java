package com.yori3o.boss_checklist.common;


import java.nio.file.Path;

import com.yori3o.boss_checklist.common.client.data.OverlapManager;
import com.yori3o.boss_checklist.common.command.SetDefeatedCommand;
import com.yori3o.boss_checklist.common.compat.Compats;
import com.yori3o.boss_checklist.common.util.ConfigFilesMover;
import com.yori3o.boss_checklist.impl.PlatformUtil;



public class BossChecklist {

    
    public static final String MOD_ID = "boss_checklist";

    public static final Path CONFIG_FOLDER = PlatformUtil.getConfigDir().resolve("boss_checklist");


    public void init() {
        ConfigFilesMover.moveConfigFiles();

        OverlapManager.loadOverlaps();

        SetDefeatedCommand.register();

        Compats.checkLoadedMods();

        if (PlatformUtil.isClient()) {
            BossChecklistClient.initClient();
        }
    }
}