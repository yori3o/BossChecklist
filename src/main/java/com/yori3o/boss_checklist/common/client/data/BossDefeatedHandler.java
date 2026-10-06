package com.yori3o.boss_checklist.common.client.data;


import com.yori3o.boss_checklist.common.client.boss.BossDefinition;
import com.yori3o.boss_checklist.common.client.boss.BossProgress;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.util.LoggerUtil;


/**
 * This class handles killing a boss on the client.
 */
public class BossDefeatedHandler {
    

    public static boolean anyoneBossKilledOnce = false;


    public static void handleBossUpdate(
            String bossId,
            String killer,
            boolean defeated,
            boolean fresh,
            boolean participated,
            ClientBossAttempt attempt
    ) {
        anyoneBossKilledOnce = true;

        BossDefinition def = BossRegistry.get(bossId);
        if (def == null) {
            LoggerUtil.warn("Information about a non-existent boss came from the server: " + bossId);
            return;
        }

        BossProgress progress = BossProgressStorage.getOrCreate(bossId);
        boolean ignoreDefeat = DynamicConfigHandler.client().ignoreUnparticipatedDefeats && !participated;

        if (defeated) {
            progress.markDefeated(killer, fresh && !ignoreDefeat, attempt);
        } else {
            progress.markNotDefeated();
        }

        if (!ignoreDefeat) {
            ClientDataSaver.setDefeated(bossId, defeated, progress);
        }

        if (DynamicConfigHandler.client().progressionMode) {
            BossNameCache.invalidate();
        }
    }
}
