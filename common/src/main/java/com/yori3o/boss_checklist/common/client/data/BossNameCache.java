package com.yori3o.boss_checklist.common.client.data;


import com.yori3o.boss_checklist.common.client.boss.BossEntry;
import com.yori3o.boss_checklist.common.client.gui.GuiConstants;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.util.TextUtil;

import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;


/**
 * This class contains the names of all bosses for display in the checklist and info menu.
 * CACHE - for full name,
 * CACHE_TRUNCATED - for use in checklist.
 */
public class BossNameCache {


    public static final LinkedHashMap<String, Component> CACHE = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Component> CACHE_TRUNCATED = new LinkedHashMap<>();
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

            if (DynamicConfigHandler.client().progressionMode && !entry.progress().isDefeated()) {
                CACHE.put(id, Component.literal("???"));
                CACHE_TRUNCATED.put(id, Component.literal("???"));
            } else {
                String key = "boss_checklist.boss." + id.replace(":", "_");
                String translated = Component.translatable(key).getString();
                String truncated = TextUtil.truncateText(translated, GuiConstants.MAX_LABEL_WIDTH).getString();
                CACHE.put(id, Component.literal(translated));
                CACHE_TRUNCATED.put(id, Component.literal(truncated));
            }
        }

        dirty = false;
    }
}
