package com.yori3o.boss_checklist.common.server.data;


import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.impl.PlatformUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;


/**
 * This class loads boss IDs from server_bosses_ids.
 */
public final class ServerBossIdsLoader {

    private static final ResourceLocation JSON_RL = ResourceLocation.fromNamespaceAndPath("boss_checklist", "server_bosses_ids.json");
    private static final Gson GSON = new Gson();
    public static final Set<String> LOADED_BOSSES = new HashSet<>();



    public static void load(ResourceManager rm) {
        
        LOADED_BOSSES.clear();
        Set<String> ALL_BOSSES = new HashSet<>();

        try {
            // get all resources with such ResourceLocation from all data packs
            for (var res : rm.getResourceStack(JSON_RL)) {
                try (Reader reader = res.openAsReader()) {
                    JsonElement root = GSON.fromJson(reader, JsonElement.class);
                    if (root == null) continue;

                    if (root.isJsonArray()) {
                        for (JsonElement el : root.getAsJsonArray()) {
                            // support for both options: element - object { "id": "..." } or primitive "namespace:id"
                            if (el.isJsonObject()) {
                                JsonObject obj = el.getAsJsonObject();
                                if (obj.has("id") && !obj.get("id").isJsonNull()) {
                                    String id = obj.get("id").getAsString().trim();
                                    if (!id.isEmpty()) ALL_BOSSES.add(id);
                                }
                            } else if (el.isJsonPrimitive()) {
                                String id = el.getAsString().trim();
                                if (!id.isEmpty()) ALL_BOSSES.add(id);
                            }
                        }
                    } 
                    else {
                        LoggerUtil.warn("Unexpected JSON type for " + JSON_RL.getPath() + " in " + res.sourcePackId());
                    }
                } catch (Exception ex) {
                    LoggerUtil.LOGGER.warn("Failed to read " + JSON_RL.getPath() + " from " + res.sourcePackId(), ex);
                }
            }
        } catch (Exception ex) {
            LoggerUtil.LOGGER.error("Unexpected error loading server boss ids!", ex);
        }

        for (String bossId : ALL_BOSSES) {
            if (PlatformUtil.isModLoaded(bossId.split(":")[0])) {
                LOADED_BOSSES.add(bossId);
            }
        }

        LoggerUtil.info("Number of loaded boss ids on the server: " + LOADED_BOSSES.size());
        LoggerUtil.info("Number of all boss ids on the server: " + ALL_BOSSES.size());
    }


    public static boolean isBoss(String bossId) {
        try {
            return LOADED_BOSSES.contains(bossId);
        } catch (Exception e) {
            LoggerUtil.LOGGER.error("There was an error checking the boss ID on the server. Perhaps the list of boss IDs hasn't loaded yet?", e);
            return false;
        }
    }

}