package com.yori3o.boss_checklist.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class BossChecklistKeyMapping {
    
    public static final KeyMapping OPEN_CHECKLIST = new KeyMapping(
    "key.boss_checklist.open_checklist", // The translation key of the name shown in the Controls screen
    InputConstants.Type.KEYSYM, // This key mapping is for Keyboards by default
    InputConstants.KEY_P, // The default keycode
    "category.boss_checklist" // The category translation key used to categorize in the Controls screen 
    );

}