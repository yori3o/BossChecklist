package com.yori3o.boss_checklist.network;

import net.minecraft.network.FriendlyByteBuf;
// only for 1.21.1+
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.RegistryAccess;

import io.netty.buffer.Unpooled;
import dev.architectury.networking.NetworkManager;


public class BossDefeatServerSend {

    @SuppressWarnings("removal")
    public static void onBossOrMinibossKilled(ServerLevel level, String bossId, String killer, Boolean bool) {

        
        RegistryAccess registryAccess = level.getServer().registryAccess(); 
    
        FriendlyByteBuf originalBuf = new FriendlyByteBuf(Unpooled.buffer()); 
    
        // only for 1.21.1+
        RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(originalBuf, registryAccess);

        registryBuf.clear();
    
        registryBuf.writeUtf(bossId + "#" + killer + "#" + bool);
        
    
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            
            NetworkManager.sendToPlayer(player, ResourceLocation.fromNamespaceAndPath("boss_checklist", "boss_defeated"), registryBuf);
        }
    }
}
