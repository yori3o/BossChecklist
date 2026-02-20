package com.yori3o.boss_checklist.fabric;


import com.yori3o.boss_checklist.common.event.EventHandler;
import com.yori3o.boss_checklist.common.client.data.BossRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;



public class BossChecklistFabricClient implements ClientModInitializer {


	@Override
	public void onInitializeClient() {
        
        ClientPlayConnectionEvents.JOIN.register((clientPacketListener, packetSender, minecraft) -> {
            EventHandler.whenJoinClient();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((clientPacketListener, minecraft) -> {
            EventHandler.whenDisconnectClient();
        });

        ClientTickEvents.START_CLIENT_TICK.register((minecraft) -> {
            EventHandler.whenClientTickPre();
        });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                @Override
                public ResourceLocation getFabricId() {
                    return ResourceLocation.fromNamespaceAndPath("boss_checklist", "client_resources_reloader");
                }

                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    BossRegistry.reload();
                }
            });
    }

}