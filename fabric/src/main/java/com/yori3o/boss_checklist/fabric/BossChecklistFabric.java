package com.yori3o.boss_checklist.fabric;


import com.yori3o.boss_checklist.common.event.EventHandler;
import com.yori3o.boss_checklist.common.server.data.ServerBossIdsLoader;
import com.yori3o.boss_checklist.common.BossChecklist;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;



public class BossChecklistFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        (new BossChecklist()).init();

        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> {
            EventHandler.whenEntityDeath(livingEntity, damageSource);
        });

        //ServerLivingEntityEvents.ALLOW_DAMAGE.register((LivingEntity livingEntity, DamageSource damageSource, float amount) -> {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((livingEntity, damageSource, amount) -> {
            EventHandler.whenEntityAllowDamage(livingEntity, damageSource, amount);
            return true;
        });

        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListenerImpl, packetSender, minecraftServer) -> {
            EventHandler.whenPlayerJoinToServer(serverGamePacketListenerImpl.getPlayer());
        });

        ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
            EventHandler.whenServerStarted(minecraftServer);
        });
        
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                @Override
                public ResourceLocation getFabricId() {
                    return new ResourceLocation("boss_checklist", "server_resources_reloader");
                }

                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    ServerBossIdsLoader.load(resourceManager);
                }
            });
    }

}