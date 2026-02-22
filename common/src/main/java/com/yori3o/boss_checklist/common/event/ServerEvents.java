package com.yori3o.boss_checklist.common.event;


import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.network.ServerSender;
import com.yori3o.boss_checklist.common.network.ClientReceiver;
import com.yori3o.boss_checklist.common.server.ServerStorage;
import com.yori3o.boss_checklist.common.server.data.BossChecklistJsonDataSaver;
import com.yori3o.boss_checklist.common.server.data.OutdatedBossDefeatedDataSaver;
import com.yori3o.boss_checklist.common.server.data.ServerBossAttempt;
import com.yori3o.boss_checklist.common.server.data.ServerBossIdsLoader;
import com.yori3o.boss_checklist.common.util.LoggerUtil;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.time.Instant;
import java.util.Map;



public class ServerEvents {


    protected static void sendDefeatedBossesToNewPlayer(ServerPlayer player) {
        // send all bosses to player
        for (String id : ServerStorage.defeatedBossesAndTheirKillers.keySet()) {

            ServerBossAttempt sa = ServerStorage.serverBossAttempts.get(id);

            String killerName = ServerStorage.defeatedBossesAndTheirKillers.get(id);

            // String one = lineOfInformation + "#false";
            String attemptTop3 = "#####";
            String globalTop3 = "#####";
            String startTime = "";
            String endTime = "";

            if (sa != null && DynamicConfigHandler.server().statisticsEnabled && !sa.endTime.equals("")) {
                attemptTop3 = sa.getTop3PlayersNamesAndDamages_SplittedByHashtag();
                startTime = sa.startTime;
                endTime = sa.endTime;
            }

            if (DynamicConfigHandler.server().statisticsEnabled) {
                globalTop3 = ServerStorage.getTop3PlayersNamesAndDamagesGlobal_SplittedByHashtag();
            }

            if (!DynamicConfigHandler.server().saveBossKillerName) {
                killerName = "";
            }

            ServerSender.sendDefeatedBossDataToPlayer(player, id, killerName, true, startTime, endTime, attemptTop3, globalTop3);
        }
    }


    protected static void loadServerData(MinecraftServer server) {
        DynamicConfigHandler.loadServer();

        // this block is needed to take the main data that was written in older versions of the mod (3.4.0-)
        OutdatedBossDefeatedDataSaver dataFromOldVersion = OutdatedBossDefeatedDataSaver.get(server.overworld());
        for (String line : dataFromOldVersion.getDefeatedBosses()) {
            String[] obsoleteFormat = line.split("#", -1);
            if (obsoleteFormat.length > 1) {
                if (ServerBossIdsLoader.isBoss(obsoleteFormat[0])) {
                    ServerStorage.defeatedBossesAndTheirKillers.put(obsoleteFormat[0], obsoleteFormat[1]);
                }
            }
        }

        File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        try {
            Map<String, String> defeatedBossesMap = BossChecklistJsonDataSaver.loadDefeatedBosses(worldDir);
            if (!defeatedBossesMap.isEmpty()) {
                for (String id : defeatedBossesMap.keySet()) {
                    if (!ServerBossIdsLoader.isBoss(id)) {
                        defeatedBossesMap.remove(id);
                    }
                }
                ServerStorage.defeatedBossesAndTheirKillers = defeatedBossesMap;
            }
            if (DynamicConfigHandler.server().statisticsEnabled) {
                ServerStorage.serverBossAttempts = BossChecklistJsonDataSaver.loadLatestBossBattles(worldDir);
                ServerStorage.playerDamages = BossChecklistJsonDataSaver.loadGlobalStatistics(worldDir);
            }
        } catch (Exception e) {
            LoggerUtil.error("Unexpected error while reading data from world folder: " + e.getMessage());
        }

    }


    protected static void whenEntityDamaged(LivingEntity entity, DamageSource source, float damageAmount) {

        String killerName = "";

        if (source.getEntity() instanceof Player player) {
            killerName = player.getName().getString();

            EntityType<?> type = entity.getType();
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);

            if (id != null) {
                String bossId = id.toString();

                if (ServerBossIdsLoader.isBoss(bossId)) {

                    ServerStorage.addPlayerDamageGlobal(killerName, damageAmount);

                    ServerBossAttempt sa = ServerStorage.serverBossAttempts.get(bossId);
                    String UUID = entity.getStringUUID();

                    if (sa == null) {
                        ServerBossAttempt new_sa = new ServerBossAttempt(bossId, UUID);
                        
                        new_sa.saveFormattedTime(Instant.now(), true);
                        new_sa.addPlayerDamage(killerName, damageAmount);

                        ServerStorage.serverBossAttempts.put(bossId, new_sa);

                        ServerStorage.serverBossAttempts.put(bossId, new_sa);
                    } else {
                        if (sa.uuid.equals(UUID)) {
                            sa.addPlayerDamage(killerName, damageAmount);
                            ServerStorage.serverBossAttempts.remove(bossId);
                            ServerStorage.serverBossAttempts.put(bossId, sa);
                        } else {
                            ServerBossAttempt new_sa = new ServerBossAttempt(bossId, UUID);
                        
                            new_sa.saveFormattedTime(Instant.now(), true);
                            new_sa.addPlayerDamage(killerName, damageAmount);

                            ServerStorage.serverBossAttempts.put(bossId, new_sa);

                            ServerStorage.serverBossAttempts.remove(bossId);
                            ServerStorage.serverBossAttempts.put(bossId, new_sa);
                        }
                    }
                } 
            }
        }
    }

    protected static void whenEntityKilled(LivingEntity entity, DamageSource source) {

        String killerName = "";

        if (source.getEntity() instanceof Player player) {
            killerName = player.getName().getString();
        }

        EntityType<?> type = entity.getType();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);

        if (id != null) {
            String bossId = id.toString();

            if (ServerBossIdsLoader.isBoss(bossId)) {
                ServerLevel level = (ServerLevel) entity.level();

                String killerName2 = "";

                if (DynamicConfigHandler.server().saveBossKillerName) {
                    killerName2 = killerName;
                }

                String startTime = "";
                String endTime = "";
                String top3attempt = "#####";
                String top3attemptGlobal = "#####";

                ServerBossAttempt sa = ServerStorage.serverBossAttempts.get(bossId);
                
                if (sa != null) {
                    String UUID = entity.getStringUUID();
                    if (sa.uuid == UUID) {
                        sa.saveFormattedTime(Instant.now(), false);
                        startTime = sa.startTime;
                        endTime = sa.endTime;
                        top3attempt = sa.getTop3PlayersNamesAndDamages_SplittedByHashtag();
                    }
                }
                if (DynamicConfigHandler.server().statisticsEnabled) {
                    top3attemptGlobal = ServerStorage.getTop3PlayersNamesAndDamagesGlobal_SplittedByHashtag();
                }
                ServerSender.sendDefeatedBossDataToAllPlayers(level, bossId, killerName2, true,  startTime, endTime, top3attempt, top3attemptGlobal);
                
                ServerStorage.defeatedBossesAndTheirKillers.put(bossId, killerName2);
            } 
        }
    }


    public static void registerPayloads() {
        ClientReceiver.register();
    }


}