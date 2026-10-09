package com.yori3o.boss_checklist.common;


import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.util.BossChecklistDataMigrator;
import com.yori3o.boss_checklist.impl.PlatformKeyMappingRegistry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;



public class BossChecklistClient {


    public static final KeyMapping OPEN_CHECKLIST = new KeyMapping(
        "key.boss_checklist.open_checklist",
        InputConstants.Type.KEYSYM,
        InputConstants.UNKNOWN.getValue(),
        "category.boss_checklist"
    );
    

    public static void initClient() {

        DynamicConfigHandler.loadClient();

        BossChecklistDataMigrator.migrateIfNeeded();
        
        PlatformKeyMappingRegistry.registerKeyMapping(OPEN_CHECKLIST);

    }
}