package com.yori3o.boss_checklist.common.client.data;


import com.yori3o.boss_checklist.common.client.boss.BossDefinition;
import com.yori3o.boss_checklist.common.client.boss.BossEntry;
import com.yori3o.boss_checklist.common.client.boss.BossProgress;

import java.util.List;


/**
 * This class is used to get information about the boss from anywhere (on the client).
 */
public class BossService {

    public static BossEntry get(String bossId) {
        BossDefinition def = BossRegistry.get(bossId);
        if (def == null) return null;

        BossProgress progress = BossProgressStorage.getOrCreate(bossId);
        return new BossEntry(def, progress);
    }

    public static List<BossEntry> all() {
        return BossRegistry.all()
                .stream()
                .map(def -> new BossEntry(def, BossProgressStorage.getOrCreate(def.id())))
                .toList();
    }
}
