package com.yori3o.boss_checklist;

import com.yori3o.boss_checklist.client.gui.BossChecklistScreen;
import com.yori3o.boss_checklist.config.ClientConfig;
import com.yori3o.boss_checklist.client.data.ClientDataSaver;
import com.yori3o.boss_checklist.client.ClientResourceReloader;
import com.yori3o.boss_checklist.network.BossDefeatedClientPacket;
import com.yori3o.boss_checklist.utils.PlatformUtils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.packs.PackType;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.ReloadListenerRegistry;

//import dev.architectury.platform.Platform; // FOR 1.20.1-





public class BossChecklistClient {

    public static final SoundEvent CHECKMARK_ADDED = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("boss_checklist", "checkmark_added"));
    public static final SoundEvent CHECKMARK_DELETED = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("boss_checklist", "checkmark_deleted"));

    
    public static boolean isProgressionMode_dynamic;

    public static final KeyMapping OPEN_CHECKLIST = new KeyMapping(
    "key.boss_checklist.open_checklist", // The translation key of the name shown in the Controls screen
    InputConstants.Type.KEYSYM, // This key mapping is for Keyboards by default
    InputConstants.KEY_P, // The default keycode
    "category.boss_checklist" // The category translation key used to categorize in the Controls screen 
    );
    

    public static void OnlyClientInit() {

        ClientConfig cc = new ClientConfig();
        cc.load();
        isProgressionMode_dynamic = cc.isProgressionMode();

        BossDefeatedClientPacket.registerPackets();

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


        KeyMappingRegistry.register(OPEN_CHECKLIST);

        // open checklist and play book sound 
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (OPEN_CHECKLIST.consumeClick()) {
                Minecraft.getInstance().setScreen(new BossChecklistScreen(false));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            }
        });

        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((level) -> {
            Minecraft.getInstance().execute(() -> {
                ClientDataSaver.UpdateCurrentWorldKey();
            });
        });

        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((level) -> {
            ClientDataSaver.defeatedBosses_InWorld.clear();
            ClientDataSaver.defeatedBossesIds_InWorld.clear();
        });

        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new ClientResourceReloader());

    }
}