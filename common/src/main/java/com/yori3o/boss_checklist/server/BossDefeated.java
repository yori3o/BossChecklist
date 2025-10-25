package com.yori3o.boss_checklist.server;

import com.yori3o.boss_checklist.BossChecklist;
import com.yori3o.boss_checklist.network.BossDefeatServerSend;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;


public final class BossDefeated {

    public static boolean isBossOrMiniboss(String bossId) {
        try {
            return ServerBossIdsLoader.server_bosses_ids_list.contains(bossId);
        } catch (Exception e) {
            return false;
        }
    }


    public void EntityKilled(LivingEntity entity, String killerName) {
        EntityType<?> type = entity.getType();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);

        ServerLevel level = (ServerLevel) entity.level();

        if (!BossChecklist.isSaveBossKiller_dynamic) {
                killerName = "";
        }

        if (id != null) {
            String bossId = id.toString();
            if (isBossOrMiniboss(bossId)) {
                BossDefeatServerSend.onBossOrMinibossKilled(level, bossId, killerName, true);

                BossDefeatedData data = BossDefeatedData.get(level.getServer().overworld());
                data.addBoss(bossId + "#" + killerName + "#true");

                BossChecklist.defeated_bosses_on_server.add(bossId + "#" + killerName + "#true");
            } 
        }
    }
}