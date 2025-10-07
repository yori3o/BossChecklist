package com.yori3o.boss_checklist;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class PlatformUtils {

    public static void registerSound(ResourceLocation id, SoundEvent event) {
        Registry.register(BuiltInRegistries.SOUND_EVENT, id, event);
    }
}