package com.yori3o.boss_checklist.fabric;


import com.yori3o.boss_checklist.common.event.EventHandler;
import com.yori3o.boss_checklist.common.server.data.ServerBossIdsLoader;
import com.yori3o.boss_checklist.common.BossChecklist;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.damagesource.DamageSource;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;



public class BossChecklistFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        (new BossChecklist()).init();

        EventHandler.whenRegisterPayloads();

        ServerLivingEntityEvents.AFTER_DEATH.register((LivingEntity entity, DamageSource source) -> {
            EventHandler.whenEntityDeath(entity, source);
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((LivingEntity entity, DamageSource source, float amount) -> {
            EventHandler.whenEntityAllowDamage(entity, source, amount);
            return true; 
        });

        ServerPlayerEvents.JOIN.register((player) -> {
            EventHandler.whenPlayerJoinToServer(player);
        });

        ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
            EventHandler.whenServerStarted(minecraftServer);
        });
        
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                @Override
                public ResourceLocation getFabricId() {
                    return ResourceLocation.fromNamespaceAndPath("boss_checklist", "server_resources_reloader");
                }

                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    ServerBossIdsLoader.load(resourceManager);
                }
            });
    }

}