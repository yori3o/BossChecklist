package com.yori3o.boss_checklist.common.network;


import com.yori3o.boss_checklist.common.client.ClientGlobalStatistics;
import com.yori3o.boss_checklist.common.client.data.BossDefeatedHandler;
import com.yori3o.boss_checklist.common.client.data.ClientBossAttempt;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.impl.PlatformNetworkHelper;

import java.util.concurrent.CompletableFuture;



public class ClientReceiver {

    public static void register() {
        PlatformNetworkHelper.registerS2C(
            BossDefeatedPayload.TYPE,
            BossDefeatedPayload.CODEC,
            (payload, context) -> {
                CompletableFuture.runAsync(() -> {
                    LoggerUtil.info("boss defeated1");
                    ClientBossAttempt ca = null;
                    if (!payload.startTime().equals("")) {
                        ca = new ClientBossAttempt(payload.bossId(), payload.startTime(), payload.endTime());
                        ca.addPlayerDamagesTop3(payload.attemptTop3());
                    }

                    BossDefeatedHandler.handleBossUpdate(
                        payload.bossId(),
                        payload.killer(),
                        payload.defeated(),
                        payload.fresh(),
                        ca
                    );
                });
                
                ClientGlobalStatistics.setTop3(payload.globalTop3().split("#", -1));
            }
        );
    }
}
