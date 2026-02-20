package com.yori3o.boss_checklist.impl;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.KeyMapping;



public class PlatformKeyMappingRegistry {


    public static final List<KeyMapping> keyMappings = new ArrayList<>();


    public static void registerKeyMapping(KeyMapping keyMapping) {
        keyMappings.add(keyMapping);
    }

}

