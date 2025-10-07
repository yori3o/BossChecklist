package com.yori3o.boss_checklist;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class PlatformUtils {

    public static void registerSound(ResourceLocation id, SoundEvent event) {
        throw new RuntimeException("Platform-specific implementation missing");
    }

    // ONLY FOR 1.20.1-
    /*public static void registerSound(ResourceLocation id) {
        throw new RuntimeException("Platform-specific implementation missing");
    }
    
    public static SoundEvent getSound(ResourceLocation id) {
        throw new RuntimeException("Platform-specific implementation missing");
    }*/
}