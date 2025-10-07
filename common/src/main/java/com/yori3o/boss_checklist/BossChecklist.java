package com.yori3o.boss_checklist;


import com.yori3o.boss_checklist.server.BossDefeatedAndLoad;
import com.yori3o.boss_checklist.server.BossDefeatedData;
import com.yori3o.boss_checklist.network.BossDefeatedPacket;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
// only for 1.21.1+
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.Set;


public class BossChecklist {
    public static final Logger LOGGER = LogManager.getLogger("boss_checklist"); //LOGGER.info("example");

    public static final String MOD_ID = "boss_checklist";


    public void init() {
        
        LifecycleEvent.SERVER_STARTED.register(this::onServerStarted);
        PlayerEvent.PLAYER_JOIN.register(this::onPlayerJoin);

        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            BossChecklistClient.OnlyClientInit();
        });

    }

    private void onServerStarted(MinecraftServer server) {
        BossDefeatedAndLoad.load(server);
        //ServerConfig.UpdateServerConfiguration();
    }

    private void onPlayerJoin(ServerPlayer player) {
        
        // always use Overworld for data saving
        ServerLevel overworld = player.server.overworld();
        // get data
        Set<String> data = BossDefeatedData.get(overworld).getDefeatedBosses();
        // idk
        RegistryAccess registryAccess = overworld.getServer().registryAccess(); 
        
        
    
        // data write to buffer and send all bosses to player
        for (String lineOfInformation : data) {

            //String bossId = lineOfInformation.split("#")[0];
            //String killer = lineOfInformation.split("#")[1];

            // one for every boss, otherwise registryBuf will be overflowing and broken

            // create FriendlyByteBuf using Netty
            FriendlyByteBuf originalBuf = new FriendlyByteBuf(Unpooled.buffer());

            // create RegistryFriendlyByteBuf with original buffer and RegistryAccess
            RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(originalBuf, registryAccess);

            //registryBuf.writeUtf(bossId + "#" + killer);
            registryBuf.writeUtf(lineOfInformation);
            NetworkManager.sendToPlayer(player, BossDefeatedPacket.BOSS_DEFEATED_PACKET, registryBuf);
        }
    }
}