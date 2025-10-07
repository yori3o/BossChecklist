package com.yori3o.boss_checklist;


import com.yori3o.boss_checklist.client.gui.BossChecklistScreen;
import com.yori3o.boss_checklist.data.BossDataClientSaver;
import com.yori3o.boss_checklist.init.BossChecklistKeyMapping;
import com.yori3o.boss_checklist.network.BossDefeatedPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
// FOR 1.20.1-
//import dev.architectury.platform.Platform;


public class BossChecklistClient {

    public static final SoundEvent CHECKMARK_ADDED = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("boss_checklist", "checkmark_added"));
    public static final SoundEvent CHECKMARK_DELETED = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("boss_checklist", "checkmark_deleted"));
    

    public static void OnlyClientInit() {

        //ClientConfig.UpdateClientConfiguration(); // There will probably be a config in the future

        // its client side but server use one variable
        BossDefeatedPacket.registerPackets();

        // FOR 1.21.1+
        PlatformUtils.registerSound(ResourceLocation.fromNamespaceAndPath("boss_checklist", "checkmark_added"), CHECKMARK_ADDED);
        PlatformUtils.registerSound(ResourceLocation.fromNamespaceAndPath("boss_checklist", "checkmark_deleted"), CHECKMARK_DELETED);

        // FOR 1.20.1-
        /*if (Platform.isForge()) {
              PlatformUtils.registerSound(new ResourceLocation("boss_checklist", "checkmark_added"));
              PlatformUtils.registerSound(new ResourceLocation("boss_checklist", "checkmark_deleted"));
        } else {
              PlatformUtils.registerSound(new ResourceLocation("boss_checklist", "checkmark_added"), CHECKMARK_ADDED);
              PlatformUtils.registerSound(new ResourceLocation("boss_checklist", "checkmark_deleted"), CHECKMARK_DELETED);
        }
        */


        KeyMappingRegistry.register(BossChecklistKeyMapping.OPEN_CHECKLIST);

        // open checklist and play book sound 
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (BossChecklistKeyMapping.OPEN_CHECKLIST.consumeClick()) {
                Minecraft.getInstance().setScreen(new BossChecklistScreen());
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            }
        });

        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register((ClientLevel level) -> {
            Minecraft.getInstance().execute(() -> {
                BossDataClientSaver.UpdateCurrentWorldKey();
                BossDataClientSaver.SetDefeatedsWhenJoiningToServer();
            });
        });
    }
}