package com.yori3o.boss_checklist.common.event;


import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.network.ServerSender;
import com.yori3o.boss_checklist.common.network.ClientReceiver;
import com.yori3o.boss_checklist.common.server.ServerStorage;
import com.yori3o.boss_checklist.common.server.data.BossChecklistJsonDataSaver;
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
import java.util.Map.Entry;
import java.util.Set;



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

            if (sa != null && DynamicConfigHandler.server().statisticsEnabled && sa.endTime != null && !sa.endTime.equals("")) {
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

            boolean participated = sa != null && sa.hasParticipant(player.getUUID().toString(), player.getName().getString());
            ServerSender.sendDefeatedBossDataToPlayer(player, id, killerName, true, startTime, endTime, attemptTop3, globalTop3, participated);
        }
    }


    protected static void loadServerData(MinecraftServer server) {
        DynamicConfigHandler.loadServer();

        File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        try {
            Map<String, String> defeatedBossesMap = BossChecklistJsonDataSaver.loadDefeatedBosses(worldDir);
            ServerStorage.defeatedBossesAndTheirKillers.clear();
            for (Entry<String, String> entry : defeatedBossesMap.entrySet()) {
                if (ServerBossIdsLoader.isBoss(entry.getKey())) {
                    ServerStorage.defeatedBossesAndTheirKillers.put(entry.getKey(), entry.getValue());
                }
            }
            ServerStorage.serverBossAttempts = BossChecklistJsonDataSaver.loadLatestBossBattles(worldDir);
            // if the boss is not a boss, we delete the attempt.
            ServerStorage.serverBossAttempts.keySet().removeIf(id -> !ServerBossIdsLoader.isBoss(id));
            if (DynamicConfigHandler.server().statisticsEnabled) {
                ServerStorage.playerDamages = BossChecklistJsonDataSaver.loadGlobalStatistics(worldDir);
            }
        } catch (Exception e) {
            LoggerUtil.errorWithException("Unexpected error while reading data from world folder: ", e);
        }

    }


    protected static void whenEntityDamaged(LivingEntity entity, DamageSource source, float damageAmount) {
        if (source.getEntity() instanceof Player player) {
            String killerName = player.getName().getString();

            EntityType<?> type = entity.getType();
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);

            if (id != null) {
                String bossId = id.toString();

                if (ServerBossIdsLoader.isBoss(bossId)) {
                    handleAttemptLogic(entity, bossId, killerName, player.getUUID().toString(), damageAmount,
                        DynamicConfigHandler.server().statisticsEnabled);
                } 
            }
        }
    }

    protected static void whenEntityKilled(LivingEntity entity, DamageSource source) {
        String killerName = "";

        String killerUuid = null;
        if (source.getEntity() instanceof Player player) {
            killerName = player.getName().getString();
            killerUuid = player.getUUID().toString();
        }

        EntityType<?> type = entity.getType();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);

        if (id != null) {
            String bossId = id.toString();

            if (ServerBossIdsLoader.isBoss(bossId)) {

                //LoggerUtil.info(String.valueOf(entity.latest));
                boolean statisticsEnabled = DynamicConfigHandler.server().statisticsEnabled;
                if (statisticsEnabled) {
                    ServerBossAttempt sa = ServerStorage.serverBossAttempts.get(bossId);
                    if (sa != null) {
                        String UUID = entity.getStringUUID();
                        if (UUID.equals(sa.uuid)) {
                            handleAttemptLogic(entity, bossId, killerName, killerUuid, sa.latestHealth, true);
                        } else {
                            handleAttemptLogic(entity, bossId, killerName, killerUuid, entity.getMaxHealth(), true);
                        }
                    } else {
                        handleAttemptLogic(entity, bossId, killerName, killerUuid, entity.getMaxHealth(), true);
                    }
                } else if (killerUuid != null) {
                    handleAttemptLogic(entity, bossId, killerName, killerUuid, 0, false);
                }

                ServerLevel level = (ServerLevel) entity.level();

                if (!DynamicConfigHandler.server().saveBossKillerName) {
                    killerName = "";
                }

                String startTime = "";
                String endTime = "";
                String top3attempt = "#####";
                String top3attemptGlobal = "#####";

                ServerBossAttempt sa = ServerStorage.serverBossAttempts.get(bossId);
                
                if (statisticsEnabled && sa != null) {
                    String UUID = entity.getStringUUID();
                    if (sa.uuid.equals(UUID)) {
                        sa.saveFormattedTime(Instant.now(), false);
                        startTime = sa.startTime;
                        endTime = sa.endTime;
                        top3attempt = sa.getTop3PlayersNamesAndDamages_SplittedByHashtag();
                    }
                }
                if (DynamicConfigHandler.server().statisticsEnabled) {
                    top3attemptGlobal = ServerStorage.getTop3PlayersNamesAndDamagesGlobal_SplittedByHashtag();
                }
                Set<String> participantUuids = sa == null ? Set.of() : sa.participantUuids();
                ServerSender.sendDefeatedBossDataToAllPlayers(level, bossId, killerName, true, startTime, endTime,
                    top3attempt, top3attemptGlobal, participantUuids);
                
                ServerStorage.defeatedBossesAndTheirKillers.put(bossId, killerName);
                ServerStorage.needsSaving = true;
            } 
        }
    }

    private static void handleAttemptLogic(LivingEntity entity, String bossId, String playerName, String playerUuid,
                                           float damageAmount, boolean collectStatistics) {
        if (playerUuid == null) return;
        if (collectStatistics) {
            ServerStorage.addPlayerDamageGlobal(playerName, damageAmount);
        }

        ServerBossAttempt sa = ServerStorage.serverBossAttempts.get(bossId);
        String UUID = entity.getStringUUID();

        if (sa == null || !sa.uuid.equals(UUID)) {
            sa = new ServerBossAttempt(bossId, UUID);
            if (collectStatistics) {
                sa.saveFormattedTime(Instant.now(), true);
            }
            ServerStorage.serverBossAttempts.put(bossId, sa);
        }

        if (!sa.hasParticipant(playerUuid)) {
            sa.addParticipant(playerUuid);
            ServerStorage.needsSaving = true;
        }

        if (collectStatistics) {
            sa.addPlayerDamage(playerName, damageAmount);
            sa.latestHealth = entity.getHealth();
        }
    }


    public static void registerPayloads() {
        ClientReceiver.register();
    }


}