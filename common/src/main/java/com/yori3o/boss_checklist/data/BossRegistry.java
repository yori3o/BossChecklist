package com.yori3o.boss_checklist.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;

import java.util.*;
import java.lang.reflect.Type;
import java.io.Reader;
import com.google.gson.reflect.TypeToken;
import com.google.gson.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dev.architectury.platform.Platform;


public class BossRegistry {
    private static final BossDataClientSaver DATA_SAVER = new BossDataClientSaver();

    private static final Map<String, BossData> BOSSES = new LinkedHashMap<>();

    public static final Logger LOGGER = LogManager.getLogger("boss_checklist");

    public static List<BossData> bossDataList;
    private static final Gson GSON = new Gson();

    public static void load() {
        Minecraft mc = Minecraft.getInstance();

        if (mc == null || mc.getResourceManager() == null) {
            LOGGER.warn("ResourceManager not ready yet!");
            return;
        }

        BOSSES.clear();
        try {
            var resourceManager = mc.getResourceManager();
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("boss_checklist", "bosses.json");

            try (Reader reader = resourceManager.openAsReader(loc)) {
                Type listType = new TypeToken<List<BossData>>() {}.getType();
                bossDataList = GSON.fromJson(reader, listType);

            } catch (Exception e) {
                LOGGER.warn("Failed to find or open bosses.json at BossRegistry!");
                return;
            }

            bossDataList.sort((b, a) -> Integer.compare(b.getPosition(), a.getPosition())); // sort from easiest to hardest

            for (BossData data : bossDataList) {
                if (Platform.isModLoaded(data.getModId())) {
                    data.SetDefeated(DATA_SAVER.isBossDefeated(data.getId()));
                    BOSSES.put(data.getId(), data);
                }
            }

            LOGGER.info("Loaded bosses: " + BOSSES.keySet());
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred while loading bosses!", e);
        }
    }

    public static BossData get(String id) {
        return BOSSES.get(id);
    }

    public static Collection<BossData> all() {
        return BOSSES.values();
    }
}