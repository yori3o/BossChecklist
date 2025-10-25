package com.yori3o.boss_checklist;

import com.yori3o.boss_checklist.commands.BossChecklistCommands;
import com.yori3o.boss_checklist.config.ServerConfig;
import com.yori3o.boss_checklist.server.BossDefeatedData;
import com.yori3o.boss_checklist.server.ServerBossIdsLoader;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf; // only for 1.21.1+
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.netty.buffer.Unpooled;

import java.util.HashSet;
import java.util.Set;


public class BossChecklist {

    public static final String MOD_ID = "boss_checklist";

    public static Set<String> defeated_bosses_on_server;

    public static boolean isSaveBossKiller_dynamic;


    public void init() {
        BossChecklistCommands.register();
        
        LifecycleEvent.SERVER_STARTED.register(this::onServerStarted);
        PlayerEvent.PLAYER_JOIN.register(this::onPlayerJoin);

        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            BossChecklistClient.OnlyClientInit();
        });

    }
    
    private void onServerStarted(MinecraftServer server) {
        ServerBossIdsLoader.load(server);
        defeated_bosses_on_server = BossDefeatedData.get(server.overworld()).getDefeatedBosses();

        ServerConfig sc = new ServerConfig();
        sc.load();
        isSaveBossKiller_dynamic = sc.isSaveBossKiller();
    }


    @SuppressWarnings("removal")
    private void onPlayerJoin(ServerPlayer player) {

        RegistryAccess registryAccess = player.server.registryAccess(); 
        
        // data write to buffer and send all bosses to player
        for (String lineOfInformation : defeated_bosses_on_server) {

            // one for every boss, otherwise registryBuf will be overflowing and broken
            // create FriendlyByteBuf using Netty
            FriendlyByteBuf originalBuf = new FriendlyByteBuf(Unpooled.buffer());

            // create RegistryFriendlyByteBuf with original buffer and RegistryAccess
            RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(originalBuf, registryAccess);

            //registryBuf.writeUtf(bossId + "#" + killer);
            registryBuf.writeUtf(lineOfInformation);
            NetworkManager.sendToPlayer(player, ResourceLocation.fromNamespaceAndPath("boss_checklist", "boss_defeated"), registryBuf);
        }
    }

    public static void removeBossFromDefeated_bosses_on_server(String value) {
        Set<String> defeated_bosses_on_serverCopy = new HashSet<>();

        for (String boss : defeated_bosses_on_server) {
            String bossId = boss.split("#")[0];

            if (!bossId.equals(value.split("#")[0])) {
                defeated_bosses_on_serverCopy.add(boss);  
            }
        }

        defeated_bosses_on_server.clear();
        defeated_bosses_on_server.addAll(defeated_bosses_on_serverCopy);
    }
}