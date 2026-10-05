package com.yori3o.boss_checklist.fabric;


import com.yori3o.boss_checklist.common.event.EventHandler;
import com.yori3o.boss_checklist.common.server.data.ServerBossIdsLoader;
import com.yori3o.boss_checklist.common.BossChecklist;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;



public class BossChecklistFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        (new BossChecklist()).init();

        EventHandler.whenRegisterPayloads();

        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> {
            EventHandler.whenEntityDeath(livingEntity, damageSource);
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((livingEntity, damageSource, amount, float2, boolean1) -> {
            EventHandler.whenEntityAllowDamage(livingEntity, damageSource, amount);
        });

        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListenerImpl, packetSender, minecraftServer) -> {
            EventHandler.whenPlayerJoinToServer(serverGamePacketListenerImpl.player);
        });

        ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
            EventHandler.whenServerStarted(minecraftServer);
        });

        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return Identifier.fromNamespaceAndPath("boss_checklist", "server_resources_reloader");
                }

                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    ServerBossIdsLoader.load(resourceManager);
                }
            });
    }

}