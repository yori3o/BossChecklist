package com.yori3o.boss_checklist.server;


import com.yori3o.boss_checklist.network.BossDefeatServerSend;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public final class BossDefeatedAndLoad {

    public static final Logger LOGGER = LogManager.getLogger("boss_checklist");

    private static final ResourceLocation JSON_RL = ResourceLocation.fromNamespaceAndPath("boss_checklist", "server_bosses_ids.json");
    private static final Gson GSON = new Gson();
    private static final Set<String> BOSSES = new HashSet<>();

    // load JSON one time when server started
    public static void load(MinecraftServer server) {
        BOSSES.clear();
        ResourceManager rm = server.getResourceManager();

        try {
            Optional<Resource> resOpt = rm.getResource(JSON_RL);
            if (resOpt.isPresent()) {
                try (Reader reader = new InputStreamReader(resOpt.get().open())) {
                    JsonElement root = GSON.fromJson(reader, JsonElement.class);
                    if (root.isJsonArray()) {
                        for (JsonElement el : root.getAsJsonArray()) {
                            if (el.isJsonObject()) {
                                // format: { "id": "wither", "mod_id": "minecraft" } but mod_id is not used anywhere yet
                                JsonObject obj = el.getAsJsonObject();
                                if (obj.has("id")) {
                                    try {
                                        BOSSES.add(obj.get("id").getAsString());
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isBoss(String bossId) {
        try {
            return BOSSES.contains(bossId);
        } catch (Exception e) {
            return false;
        }
    }


    public void EntityKilled(LivingEntity entity, String killerName) {
        EntityType<?> type = entity.getType();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);

        ServerLevel level = (ServerLevel) entity.level();


        //if (!ServerConfig.saveBossKiller) { // There will probably be a config in the future
        //    killerName = null;
        //}

        if (id != null) {
            String bossId = id.toString().split(":")[1];
            if (isBoss(bossId)) {
                BossDefeatServerSend.onBossKilled(level, bossId, killerName);
            } 
        }
    }
}