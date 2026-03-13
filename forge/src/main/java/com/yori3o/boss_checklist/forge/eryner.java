package com.yori3o.boss_checklist.forge;


import com.yori3o.boss_checklist.common.event.EventHandler;
import com.yori3o.boss_checklist.impl.PlatformKeyMappingRegistry;
import com.yori3o.boss_checklist.common.BossChecklist;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;



@EventBusSubscriber(modid = BossChecklist.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class eryner {



    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        EventHandler.whenClientTickStart();
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        EventHandler.whenDisconnectClient();
    }
    
    @SubscribeEvent
    public static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        EventHandler.whenJoinClient();
    }
    /*@SubscribeEvent
    public static void onRegisterKeymappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping keyMapping : PlatformKeyMappingRegistry.keyMappings) {
            event.register(keyMapping);
        }
    }

    @SubscribeEvent
    public static void registerClientReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ClientReloadListener());
    }*/

}