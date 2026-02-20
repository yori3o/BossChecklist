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

    public static final Object FILE_IO_LOCK = new Object();


    public static void handleBossUpdate(
            String bossId,
            String killer,
            boolean defeated,
            boolean fresh,
            ClientBossAttempt attempt
    ) {
        synchronized (FILE_IO_LOCK) {

            anyoneBossKilledOnce = true;

            BossDefinition def = BossRegistry.get(bossId);
            if (def == null) {
                LoggerUtil.warn("Boss not registered: " + bossId);
                return;
            }

            BossProgress progress = BossProgressStorage.getOrCreate(bossId);

            if (defeated) {
                progress.markDefeated(killer, fresh, attempt);
            } else {
                progress.markNotDefeated();
            }
            progress.markDefeatedClient(defeated);

            ClientDataSaver.setDefeated(bossId.toString(), defeated);

            if (DynamicConfigHandler.progressionMode_dynamic) {
                BossNameCache.invalidate();
            }
        }
    }
}
