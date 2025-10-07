package com.yori3o.boss_checklist.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yori3o.boss_checklist.server.BossDefeatedData;

import net.minecraft.network.FriendlyByteBuf;
// only for 1.21.1+
import net.minecraft.network.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.RegistryAccess;
import dev.architectury.networking.NetworkManager;


public class BossDefeatServerSend {
    public static final Logger LOGGER = LogManager.getLogger("boss_checklist");

    public static void onBossKilled(ServerLevel level, String bossId, String killer) {
    
        RegistryAccess registryAccess = level.getServer().registryAccess(); 
    
        FriendlyByteBuf originalBuf = new FriendlyByteBuf(Unpooled.buffer()); 
    
        // only for 1.21.1+
        RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(originalBuf, registryAccess);

        registryBuf.clear();
    
        registryBuf.writeUtf(bossId + "#" + killer);
        
    
        for (ServerPlayer player : level.players()) {
            
            NetworkManager.sendToPlayer(player, BossDefeatedPacket.BOSS_DEFEATED_PACKET, registryBuf);
        }


        BossDefeatedData data = BossDefeatedData.get(level.getServer().overworld());
        data.addBoss(bossId + "#" + killer);
    }
}
