package com.yori3o.boss_checklist.common.sound;


import com.yori3o.boss_checklist.impl.PlatformSoundRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;


/**
 * You can register a sound here and take it from here later.
 */
public class SoundRegistry {

    
    public static final SoundEvent CHECKMARK_ADDED = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("boss_checklist", "checkmark_added"));
    public static final SoundEvent CHECKMARK_DELETED = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("boss_checklist", "checkmark_deleted"));


    public static void register() {
        PlatformSoundRegistry.registerSound(CHECKMARK_ADDED);
        PlatformSoundRegistry.registerSound(CHECKMARK_DELETED);
    }
}