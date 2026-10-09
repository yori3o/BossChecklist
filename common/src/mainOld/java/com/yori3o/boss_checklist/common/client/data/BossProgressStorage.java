package com.yori3o.boss_checklist.common.client.data;


import com.yori3o.boss_checklist.common.client.boss.BossProgress;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * This class is used for internal mechanisms.
 */
public class BossProgressStorage {


    private static final Map<String, BossProgress> PROGRESS = new HashMap<>();


    public static BossProgress getOrCreate(String bossId) {
        return PROGRESS.computeIfAbsent(bossId, id -> new BossProgress());
    }

    public static boolean isDefeated(String bossId) {
        return getOrCreate(bossId).isDefeated();
    }

    public static void clear() {
        PROGRESS.clear();
    }

    public static Collection<String> defeatedBosses() {
        return PROGRESS.entrySet()
                .stream()
                .filter(e -> e.getValue().isDefeated())
                .map(Map.Entry::getKey)
                .toList();
    }
}
