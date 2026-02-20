package com.yori3o.boss_checklist.common.client.data;


import com.yori3o.boss_checklist.common.client.boss.BossDefinition;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.impl.PlatformUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;

import com.google.gson.reflect.TypeToken;
import com.google.gson.*;
import java.lang.reflect.Field;
import java.util.*;
import java.lang.reflect.Type;
import java.io.Reader;


/**
 * This class is used to register bosses on the client from all bosses.json.
 */
public class BossRegistry {


    private static final ResourceLocation BOSSES_JSON = ResourceLocation.fromNamespaceAndPath("boss_checklist", "bosses.json");
    private static final Gson GSON = new Gson();

    private static final Map<String, BossDefinition> DEFINITIONS = new LinkedHashMap<>();


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
                loaded = GSON.fromJson(reader, listType);

                LoggerUtil.info("Loading bosses.json from " + res.sourcePackId());
            } catch (Exception e) {
                LoggerUtil.LOGGER.warn("Failed to read bosses.json from " + res.sourcePackId(), e);
            }
        }
        
        loaded.sort(Comparator.comparingDouble(BossDefinition::position));

        // merge replace
        Map<String, BossDefinition> merged = new LinkedHashMap<>();

        for (BossDefinition def : loaded) {
            if (def == null || def.id() == null) {
                LoggerUtil.error("BossDefinition without id!");
                continue;
            }

            BossDefinition existing = merged.get(def.id());

            if (existing == null || def.replace) {
                merged.put(def.id(), def);
            } else {
                // merge fields
                mergeDefinitions(existing, def);
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

        LoggerUtil.info("Count of registered bosses: " + DEFINITIONS.size());
    }


    public static BossDefinition get(String id) {
        return DEFINITIONS.get(id);
    }


    public static Collection<BossDefinition> all() {
        return DEFINITIONS.values();
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
            LoggerUtil.LOGGER.error("Error when merging BossDefinition fields", e);
        }
    }
}
