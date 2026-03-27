package com.yori3o.boss_checklist.impl;


import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;



public class PlatformSoundRegistry {


    public static void registerSound(SoundEvent soundEvent) {
        Registry.register(BuiltInRegistries.SOUND_EVENT, soundEvent.location(), soundEvent);
    }
    
}