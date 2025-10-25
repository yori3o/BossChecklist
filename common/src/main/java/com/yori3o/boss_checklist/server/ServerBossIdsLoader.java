package com.yori3o.boss_checklist.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yori3o.boss_checklist.utils.LoggerUtil;

import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

/*import java.util.Optional; // For old realization
import java.io.InputStreamReader;
import net.minecraft.server.packs.resources.Resource;*/


public final class ServerBossIdsLoader {

    private static final ResourceLocation JSON_RL = ResourceLocation.fromNamespaceAndPath("boss_checklist", "server_bosses_ids.json");
    private static final Gson GSON = new Gson();
    public static final Set<String> server_bosses_ids_list = new HashSet<>();
    public static final Set<String> server_bosses_ids_list_only_loaded_mods = new HashSet<>();


  public static void load(MinecraftServer server) {
    
    server_bosses_ids_list.clear();
    ResourceManager rm = server.getResourceManager();

    try {
        // get all resources with such ResourceLocation from all data packs
        for (var res : rm.getResourceStack(JSON_RL)) {
            try (Reader reader = res.openAsReader()) {
                JsonElement root = GSON.fromJson(reader, JsonElement.class);
                if (root == null) continue;

                // if is Json Array — перебираем элементы
                if (root.isJsonArray()) {
                    for (JsonElement el : root.getAsJsonArray()) {
                        // поддерживаем оба варианта: элемент — объект { "id": "..." } или примитив "namespace:id"
                        if (el.isJsonObject()) {
                            JsonObject obj = el.getAsJsonObject();
                            if (obj.has("id") && !obj.get("id").isJsonNull()) {
                                String id = obj.get("id").getAsString().trim();
                                if (!id.isEmpty()) server_bosses_ids_list.add(id);
                            }
                        } else if (el.isJsonPrimitive()) {
                            String id = el.getAsString().trim();
                            if (!id.isEmpty()) server_bosses_ids_list.add(id);
                        }
                    }
                } 
                else {
                    LoggerUtil.LOGGER.warn("Unexpected JSON type for server_bosses_ids in " + res.sourcePackId());
                }
            } catch (Exception ex) {
                LoggerUtil.LOGGER.warn("Failed to read server_bosses_ids.json from " + res.sourcePackId(), ex);
            }
        }
    } /*catch (NoSuchMethodError | UnsupportedOperationException fallback) {
        // На случай, если у ResourceManager нет getResourceStack (редко), попробуем старый вариант
        LoggerUtil.LOGGER.warn("getResourceStack unavailable, falling back to single resource lookup");
        try {
            Optional<Resource> resOpt = rm.getResource(JSON_RL);
            if (resOpt.isPresent()) {
                try (Reader reader = new InputStreamReader(resOpt.get().open())) {
                    JsonElement root = GSON.fromJson(reader, JsonElement.class);
                    if (root != null && root.isJsonArray()) {
                        for (JsonElement el : root.getAsJsonArray()) {
                            if (el.isJsonObject()) {
                                JsonObject obj = el.getAsJsonObject();
                                if (obj.has("id") && !obj.get("id").isJsonNull()) {
                                    String id = obj.get("id").getAsString().trim();
                                    if (!id.isEmpty()) server_bosses_ids_list.add(id);
                                }
                            } else if (el.isJsonPrimitive()) {
                                String id = el.getAsString().trim();
                                if (!id.isEmpty()) server_bosses_ids_list.add(id);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LoggerUtil.LOGGER.error("Error while fallback-loading server_bosses_ids", ex);
        }
    } */catch (Exception ex) {
        LoggerUtil.LOGGER.error("Unexpected error loading server boss ids", ex);
    }

    LoggerUtil.LOGGER.info("Loaded server boss ids: " + server_bosses_ids_list.size());

    for (String bossId : server_bosses_ids_list) {
        if (Platform.isModLoaded(bossId.split(":")[0])) {
            server_bosses_ids_list_only_loaded_mods.add(bossId);
        }
    }
  }

}