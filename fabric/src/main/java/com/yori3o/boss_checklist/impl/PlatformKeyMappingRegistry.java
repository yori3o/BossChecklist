package com.yori3o.boss_checklist.impl;


import net.minecraft.client.KeyMapping;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;



public class PlatformKeyMappingRegistry {

    
    public static void registerKeyMapping(KeyMapping keyMapping) {
        KeyBindingHelper.registerKeyBinding(keyMapping);
    }
 
}

