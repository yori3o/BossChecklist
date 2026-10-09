package com.yori3o.boss_checklist.common.network;


import com.yori3o.boss_checklist.impl.PlatformNetworkHelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;



public class ServerSender {

    /**
     * This method sends a packet with boss data to all players on the server.
     */
    public static void sendDefeatedBossDataToAllPlayers(ServerLevel level, String bossId, String killer, boolean defeated,
                    String startTime, String endTime, String attemptTop3, String globalTop3, Set<String> participantUuids) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            boolean participated = participantUuids.contains(player.getUUID().toString());
            BossDefeatedPayload payload = new BossDefeatedPayload(
                bossId, killer, defeated, true, startTime, endTime, attemptTop3, globalTop3, participated
            );
            PlatformNetworkHelper.sendToPlayer(player, payload);
        }
    }

    /**
     * This method sends a packet with data about one defeated boss to one player.
     */
    public static void sendDefeatedBossDataToPlayer(ServerPlayer player, String bossId, String killer, boolean defeated,
                    String startTime, String endTime, String attemptTop3, String globalTop3, boolean participated) {

        BossDefeatedPayload payload = new BossDefeatedPayload(
                bossId, killer, defeated, false, startTime, endTime, attemptTop3, globalTop3, participated
        );

        PlatformNetworkHelper.sendToPlayer(player, payload);
    }
}
