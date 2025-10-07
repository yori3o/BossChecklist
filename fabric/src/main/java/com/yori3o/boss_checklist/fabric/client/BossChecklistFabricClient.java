package com.yori3o.boss_checklist.fabric.client;

import com.yori3o.boss_checklist.data.BossRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class BossChecklistFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
         ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            BossRegistry.load();
        });            
    }
}
