package com.yori3o.boss_checklist.common.client.data;


import com.yori3o.boss_checklist.common.client.boss.BossEntry;
import com.yori3o.boss_checklist.common.client.gui.BossChecklistScreen;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.util.TextUtil;

import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;


/**
 * This class contains the names of all bosses for display in the checklist and info menu.
 */
public class BossNameCache {


    public static final LinkedHashMap<String, Component> CACHE = new LinkedHashMap<>();
    private static boolean dirty = true;


    public static void invalidate() {
        dirty = true;
    }

    public static Component getName(String bossId) {
        if (dirty) rebuild();
        return CACHE.getOrDefault(bossId, Component.literal("???"));
    }

    public static void rebuild() {
        CACHE.clear();

        for (BossEntry entry : BossService.all()) {
            String id = entry.id();

            if (DynamicConfigHandler.progressionMode_dynamic && !entry.progress().isDefeated()) {
                CACHE.put(id, Component.literal("???"));
            } else {
                String key = "boss_checklist.boss." + id.replace(":", "_");
                String translated = Component.translatable(key).getString();
                String truncated = TextUtil.truncateText(translated, BossChecklistScreen.MAX_LABEL_WIDTH).getString();
                CACHE.put(id, Component.literal(truncated));
            }
        }

        dirty = false;
    }
}
