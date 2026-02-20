package com.yori3o.boss_checklist.common;


import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.util.BossChecklistDataMigrator;
import com.yori3o.boss_checklist.impl.PlatformKeyMappingRegistry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;



public class BossChecklistClient {


    public static final KeyMapping OPEN_CHECKLIST = new KeyMapping(
        "key.boss_checklist.open_checklist", // The translation key of the name shown in the Controls screen
        InputConstants.Type.KEYSYM, // This key mapping is for Keyboards by default
        InputConstants.UNKNOWN.getValue(), // The default keycode
        "category.boss_checklist" // The category translation key used to categorize in the Controls screen 
    );
    

    public static void initClient() {

        DynamicConfigHandler.ClientConfigLoad();

        BossChecklistDataMigrator.migrateIfNeeded();
        
        PlatformKeyMappingRegistry.registerKeyMapping(OPEN_CHECKLIST);

    }
}