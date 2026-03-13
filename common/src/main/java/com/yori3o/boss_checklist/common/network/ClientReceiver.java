package com.yori3o.boss_checklist.common.network;


import com.yori3o.boss_checklist.common.client.ClientGlobalStatistics;
import com.yori3o.boss_checklist.common.client.data.BossDefeatedHandler;
import com.yori3o.boss_checklist.common.client.data.ClientBossAttempt;

import dev.architectury.networking.NetworkManager;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;



public class ClientReceiver {


    private static final Object LOCK = new Object();


    public static void register() {
        NetworkManager.registerReceiver(
            NetworkManager.s2c(),
            new ResourceLocation("boss_checklist", "boss_defeated"),
            (payload, context) -> {

                String bossId = payload.readUtf();
                String killerName = payload.readUtf();
                boolean defeated = payload.readBoolean();
                boolean fresh = payload.readBoolean();
                String startTime = payload.readUtf();
                String endTime = payload.readUtf();
                String attemptTop3 = payload.readUtf();
                String globalTop3 = payload.readUtf();

                CompletableFuture.runAsync(() -> {
                    synchronized (LOCK) {
                        ClientBossAttempt ca = null;
                        if (! startTime .equals("")) {
                            ca = new ClientBossAttempt( bossId ,  startTime ,  endTime );
                            ca.addPlayerDamagesTop3( attemptTop3 );
                        }

                        BossDefeatedHandler.handleBossUpdate(
                             bossId ,
                             killerName ,
                             defeated ,
                             fresh ,
                            ca
                        );
                    }
                });
                
                ClientGlobalStatistics.setTop3( globalTop3 .split("#", -1));
            }
        );
    }
}
