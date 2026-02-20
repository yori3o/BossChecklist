package com.yori3o.boss_checklist.neoforge;


import com.yori3o.boss_checklist.common.event.EventHandler;
import com.yori3o.boss_checklist.impl.PlatformNetworkHelper;
import com.yori3o.boss_checklist.common.BossChecklist;

import net.minecraft.server.level.ServerPlayer;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;



@EventBusSubscriber(modid = BossChecklist.MOD_ID)
public class BossChecklistNeoForgeServerEvents {


    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EventHandler.whenPlayerJoinToServer(player);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        EventHandler.whenServerStarted(event.getServer());
    }

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ServerReloadListener());
    }

    @SubscribeEvent
    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PlatformNetworkHelper.init(event);
        EventHandler.whenRegisterPayloads();
    }

}