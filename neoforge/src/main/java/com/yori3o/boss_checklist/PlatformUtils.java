package com.yori3o.boss_checklist;

import java.util.ArrayList;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;


public class PlatformUtils {

    public static void registerSound(ResourceLocation id, SoundEvent event) {
        ModSoundsHolder.addSound(id, event);
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.SOUND_EVENT) {
            event.register(Registries.SOUND_EVENT, helper -> {
                for (var entry : ModSoundsHolder.SOUNDS) {
                    helper.register(entry.id, entry.sound);
                }
            });
        }
    }

    public static class ModSoundsHolder {
        public static final List<SoundEntry> SOUNDS = new ArrayList<>();

        public static void addSound(ResourceLocation id, SoundEvent sound) {
            SOUNDS.add(new SoundEntry(id, sound));
        }

        public record SoundEntry(ResourceLocation id, SoundEvent sound) {}
    }
}

