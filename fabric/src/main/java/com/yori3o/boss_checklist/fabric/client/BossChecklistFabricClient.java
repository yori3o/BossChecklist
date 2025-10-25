package com.yori3o.boss_checklist.fabric.client;

import com.yori3o.boss_checklist.BossChecklistClient;
//import com.yori3o.boss_checklist.config.ClientConfig;
//import com.yori3o.boss_checklist.client.data.BossRegistry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;


public class BossChecklistFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
         ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {

            /*if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
                BossChecklistClient.hideUndefeatedBossInfo = ClientConfig.hideUndefeatedBossInfo; }*/

            //BossRegistry.load();
        });            
    }
}
