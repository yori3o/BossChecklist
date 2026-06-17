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
            ClientBossAttempt attempt
    ) {
        anyoneBossKilledOnce = true;

        BossDefinition def = BossRegistry.get(bossId);
        if (def == null) {
            LoggerUtil.warn("Information about a non-existent boss came from the server: " + bossId);
            return;
        }

        BossProgress progress = BossProgressStorage.getOrCreate(bossId);

        if (defeated) {
            progress.markDefeated(killer, fresh, attempt);
        } else {
            progress.markNotDefeated();
        }

        ClientDataSaver.setDefeated(bossId.toString(), defeated, progress);

        if (DynamicConfigHandler.client().progressionMode) {
            BossNameCache.invalidate();
        }
    }
}
