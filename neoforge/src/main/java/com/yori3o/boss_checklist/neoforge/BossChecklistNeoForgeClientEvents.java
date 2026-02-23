package com.yori3o.boss_checklist.neoforge;


import com.yori3o.boss_checklist.common.event.EventHandler;
import com.yori3o.boss_checklist.impl.PlatformKeyMappingRegistry;
import com.yori3o.boss_checklist.common.BossChecklist;

import net.minecraft.client.KeyMapping;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;



@EventBusSubscriber(modid = BossChecklist.MOD_ID, value = Dist.CLIENT)
public class BossChecklistNeoForgeClientEvents {


    @SubscribeEvent
    public static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        EventHandler.whenJoinClient();
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        EventHandler.whenDisconnectClient();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        EventHandler.whenClientTickStart();
    }

    @SubscribeEvent
    public static void onRegisterKeymappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping keyMapping : PlatformKeyMappingRegistry.keyMappings) {
            event.register(keyMapping);
        }
    }

    @SubscribeEvent
    public static void registerClientReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ClientReloadListener());
    }

}