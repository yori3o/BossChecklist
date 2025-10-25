package com.yori3o.boss_checklist.client.data;

import com.yori3o.boss_checklist.utils.LoggerUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.util.*;
import java.lang.reflect.Type;
import java.io.Reader;
import com.google.gson.reflect.TypeToken;
import com.google.gson.*;
import dev.architectury.platform.Platform;


public class BossRegistry {
    private static final ClientDataSaver DATA_SAVER = new ClientDataSaver();

    private static final Map<String, BossData> BOSSES = new LinkedHashMap<>();

    public static List<BossData> bossDataList;
    private static final Gson GSON = new Gson();

    public static void load() {
    Minecraft mc = Minecraft.getInstance();
    if (mc == null || mc.getResourceManager() == null) {
        LoggerUtil.LOGGER.warn("ResourceManager not ready yet!");
        return;
    }

    BOSSES.clear();

    try {
        var resourceManager = mc.getResourceManager();
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("boss_checklist", "bosses.json");

        List<BossData> allBosses = new ArrayList<>();

        for (var res : resourceManager.getResourceStack(loc)) {
            try (Reader reader = res.openAsReader()) {
                Type listType = new TypeToken<List<BossData>>() {}.getType();
                List<BossData> partial = GSON.fromJson(reader, listType);
                if (partial != null) allBosses.addAll(partial);
                LoggerUtil.LOGGER.info("Loaded bosses.json from " + res.sourcePackId());
            } catch (Exception e) {
                LoggerUtil.LOGGER.warn("Failed to read bosses.json from " + res.sourcePackId(), e);
            }
        }

        //allBosses.forEach(BossData::applyReplaceDefault);

        Map<String, BossData> merged = new LinkedHashMap<>();
        for (BossData data : allBosses) {
            //if (data.getId() == null) continue;

            Boolean replace = Boolean.TRUE.equals(data.replace);
            BossData existing = merged.get(data.getId());

            if (existing == null || replace) {
                merged.put(data.getId(), data);
            } else {
                if (!(existing.replace)) {
                    mergeFields(existing, data);
                }
            }
        }

        bossDataList = new ArrayList<>(merged.values());
        bossDataList.forEach(BossData::applyDefaults);
        bossDataList.sort(Comparator.comparingDouble(BossData::getPosition));

        for (BossData data : bossDataList) {
            if (Platform.isModLoaded(data.getModId())) {
                data.SetDefeated(DATA_SAVER.isBossDefeated(data.getId()));
                BOSSES.put(data.getId(), data);
            }
        }

        LoggerUtil.LOGGER.info("Loaded bosses: " + BOSSES.keySet());
    } catch (Exception e) {
        LoggerUtil.LOGGER.error("An unexpected error occurred while loading bosses!", e);
    }
}

    public static BossData get(String id) {
        return BOSSES.get(id);
    }

    public static Collection<BossData> all() {
        return BOSSES.values();
    }


    @SuppressWarnings("unchecked")
    private static void mergeFields(BossData base, BossData addition) {

        if (base == null || addition == null) return;

        try {
            for (Field field : BossData.class.getDeclaredFields()) {
                field.setAccessible(true);

                // Пропускаем служебное поле "replace"
                if (field.getName().equals("replace")) continue;

                Object addValue = field.get(addition);
                Object baseValue = field.get(base);

                if (addValue == null) continue; // пропускаем null из дополнения

                // Если это список (например, drops), то объединим, а не перезапишем
                if (List.class.isAssignableFrom(field.getType())) {
                    List<?> addList = (List<?>) addValue;
                    if (addList.isEmpty()) continue;

                    if (baseValue == null) {
                        field.set(base, new ArrayList<>(addList));
                    } else {
                        ((List<Object>) baseValue).addAll(addList);
                    }
                } else {
                    // Для всех остальных типов просто перезаписываем
                    field.set(base, addValue);
                }
            }
        } catch (Exception e) {
            LoggerUtil.LOGGER.error("Error merging BossData fields", e);
        }
    }
}