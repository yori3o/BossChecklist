package com.yori3o.boss_checklist.common.event;


import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.CompletableFuture;


/**
 * These methods are called from platform events.
 * They also check configuration settings and separate client and server logic.
 */
public class EventHandler {


    public static void whenEntityDeath(LivingEntity entity, DamageSource source) {
        if (DynamicConfigHandler.server().asyncLogic) {
            CompletableFuture.runAsync(() -> {
                ServerEvents.whenEntityKilled(entity, source);
            });
        } else {
            ServerEvents.whenEntityKilled(entity, source);
        }
    }

    public static void whenEntityAllowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (DynamicConfigHandler.server().statisticsEnabled) {
            if (DynamicConfigHandler.server().asyncLogic) {
                CompletableFuture.runAsync(() -> {
                    ServerEvents.whenEntityDamaged(entity, source, amount);
                });
            } else {
                ServerEvents.whenEntityDamaged(entity, source, amount);
            }
        }
    }

    public static void whenPlayerJoinToServer(ServerPlayer player) {
        if (DynamicConfigHandler.server().asyncLogic) {
            CompletableFuture.runAsync(() -> {
                ServerEvents.sendDefeatedBossesToNewPlayer(player);
            });
        } else {
            ServerEvents.sendDefeatedBossesToNewPlayer(player);    
        }
    }

    public static void whenServerStarted(MinecraftServer minecraftServer) {
        ServerEvents.loadServerData(minecraftServer);
    }

    public static void whenJoinClient() {
        ClientEvents.updateClientBossDefeatedData();
    }

    public static void whenDisconnectClient() {
        ClientEvents.clearClientCache();
    }

    public static void whenClientTickStart() {
        ClientEvents.checkKeybindPressed();
    }

    public static void whenRegisterPayloads() {
        ServerEvents.registerPayloads();
    }

}