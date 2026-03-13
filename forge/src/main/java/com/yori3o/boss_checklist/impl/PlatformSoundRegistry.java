package com.yori3o.boss_checklist.impl;


import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;
import java.util.ArrayList;



public class PlatformSoundRegistry {


    public static final List<SoundEvent> SOUNDS = new ArrayList<>();


    public static void registerSound(SoundEvent soundEvent) {
        SOUNDS.add(soundEvent);
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.SOUND_EVENT) {
            event.register(Registries.SOUND_EVENT, helper -> {
                for (SoundEvent soundEvent : SOUNDS) {
                    helper.register(soundEvent.getLocation(), soundEvent);
                }
            });
        }
    }

}

