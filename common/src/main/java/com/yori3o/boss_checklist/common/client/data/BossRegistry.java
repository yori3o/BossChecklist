package com.yori3o.boss_checklist.common.client.data;


import com.yori3o.boss_checklist.common.client.boss.BossDefinition;
import com.yori3o.boss_checklist.common.client.gui.BossChecklistScreen;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.common.util.MoveCursorUtil;
import com.yori3o.boss_checklist.impl.PlatformUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;


/**
 * This class is used to register bosses on the client from all bosses.json.
 */
public class BossRegistry {


    private static final ResourceLocation BOSSES_JSON = new ResourceLocation("boss_checklist", "bosses.json");
    private static final Gson GSON = new Gson();

    public static final Map<String, BossDefinition> DEFINITIONS = new LinkedHashMap<>();


    public static void reload() {
        DEFINITIONS.clear();

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getResourceManager() == null) {
            LoggerUtil.warn("ResourceManager not ready yet!");
            return;
        }

        List<BossDefinition> loaded = new ArrayList<>();

        for (var res : mc.getResourceManager().getResourceStack(BOSSES_JSON)) {
            try (Reader reader = res.openAsReader()) {
                Type listType = new TypeToken<List<BossDefinition>>() {}.getType();
                loaded.addAll(GSON.fromJson(reader, listType));

                //LoggerUtil.info("Loading bosses.json from " + res.sourcePackId());
            } catch (Exception e) {
                LoggerUtil.errorWithException("Failed to read bosses.json from " + res.sourcePackId(), e);
            }
        }
        OverlapManager.loadOverlaps();
        loaded.addAll(OverlapManager.OVERLAP_DEFINITIONS.values());

        if (!OverlapManager.OVERLAP_POSITIONS.isEmpty()) {
            for (BossDefinition def : loaded) {
                if (OverlapManager.OVERLAP_POSITIONS.containsKey(def.id())) {
                    def.setPosition(OverlapManager.OVERLAP_POSITIONS.get(def.id()));
                }
            }
        }

        loaded.sort(Comparator.comparingDouble(BossDefinition::position));

        Map<String, BossDefinition> merged = new LinkedHashMap<>();

        for (BossDefinition def : loaded) {
            if (def == null || def.id() == null) {
                LoggerUtil.error("BossDefinition without id!");
                continue;
            }

            BossDefinition existing = merged.get(def.id());

            if (existing == null) {
                merged.put(def.id(), def);
            } else {
                if (def.replace) {
                    merged.put(def.id(), def);
                } else if (existing.replace) {
                    merged.put(existing.id(),existing);
                } else {
                    mergeDefinitions(existing, def);
                }
            }
        }

        merged.values().forEach(BossDefinition::applyDefaults);

        merged.values().forEach(BossDefinition::applyPostLoadFixes);

        // add only loaded mods
        for (BossDefinition def : merged.values()) {
            if (PlatformUtil.isModLoaded(def.modId())) {
                DEFINITIONS.put(def.id(), def);
            }
        }

        BossNameCache.rebuild();

        LoggerUtil.info("Number of registered bosses: " + DEFINITIONS.size());
        //LoggerUtil.info("Number of all bosses: " + merged.size());
    }


    public static BossDefinition get(String id) {
        return DEFINITIONS.get(id);
    }


    public static Collection<BossDefinition> all() {
        return DEFINITIONS.values();
    }


    public static boolean isBoss(String id) {
        return DEFINITIONS.keySet().contains(id);
    }


    @SuppressWarnings("unchecked")
    private static void mergeDefinitions(BossDefinition base, BossDefinition addition) {

        if (base == null || addition == null) return;

        try {
            for (Field field : BossDefinition.class.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.getName().equals("replace")) continue;

                Object addValue = field.get(addition);
                Object baseValue = field.get(base);

                if (addValue == null) continue;

                // if it's a list (like drops), then we'll merge them instead of overwriting them
                if (List.class.isAssignableFrom(field.getType())) {
                    List<?> addList = (List<?>) addValue;
                    if (addList.isEmpty()) continue;

                    if (baseValue == null) {
                        field.set(base, new ArrayList<>(addList));
                    } else {
                        ((List<Object>) baseValue).addAll(addList);
                    }
                } else {
                    // for all other types, simply rewrite
                    field.set(base, addValue);
                }
            }
        } catch (Exception e) {
            LoggerUtil.errorWithException("Error when merging BossDefinition fields", e);
        }
    }

    public static boolean moveBossPosition(boolean up, BossDefinition boss, BossChecklistScreen screen, boolean moveCursor, long window) {
        List<BossDefinition> list = new ArrayList<>(DEFINITIONS.values());

        int index = list.indexOf(boss);
        if (index == -1) return false;

        int newIndex = up ? index - 1 : index + 1;
        if (newIndex < 0 || newIndex >= list.size()) return false;

        Collections.swap(list, index, newIndex);

        // обновляем порядок
        DEFINITIONS.clear();
        for (BossDefinition def : list) {
            DEFINITIONS.put(def.id(), def);
        }

        float posOfBoss = list.get(newIndex).position();
        float posOfSwappedBoss = list.get(index).position();
        OverlapManager.addPositionOverlap(list.get(index).id(), posOfBoss);
        OverlapManager.addPositionOverlap(boss.id(), posOfSwappedBoss);
        list.get(index).setPosition(posOfBoss);
        boss.setPosition(posOfSwappedBoss);
        OverlapManager.savePositionOverlap(OverlapManager.OVERLAP_POSITIONS);

        if (moveCursor) {
            // старые координаты
            int oldPage = index / 7;
            int oldSpread = oldPage / 2;
            int oldSlot = index % 7;

            // новые координаты
            int newPage = newIndex / 7;
            int newSpread = newPage / 2;
            int newSlot = newIndex % 7;

            int rowDelta = newSlot - oldSlot;
            int pageDelta = newPage - oldPage;
            int spreadDelta = newSpread - oldSpread;

            if (spreadDelta != 0) pageDelta = -pageDelta;

            MoveCursorUtil.moveCursorInChecklist(window, rowDelta, pageDelta);
            screen.flipPage(spreadDelta);
        }

        return true;
    }
}
