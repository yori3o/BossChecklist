package com.yori3o.boss_checklist.impl;


import java.util.function.BiConsumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;



public class PlatformNetworkHelper {

    public static <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T payload) {
        throw new RuntimeException("Platform-specific implementation missing");
    }

    public static <T extends CustomPacketPayload> void registerS2C(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            BiConsumer<T, PayloadContext> handler
    ) {
        throw new RuntimeException("Platform-specific implementation missing");
    }

    public interface PayloadContext {
        void enqueue(Runnable runnable);
        Player getPlayer();
    }
}