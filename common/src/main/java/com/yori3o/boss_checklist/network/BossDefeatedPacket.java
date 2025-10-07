package com.yori3o.boss_checklist.network;

import net.minecraft.resources.ResourceLocation;
import dev.architectury.networking.NetworkManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yori3o.boss_checklist.data.BossDataClientSaver;


public class BossDefeatedPacket {
    public static final Logger LOGGER = LogManager.getLogger("boss_checklist"); //LOGGER.info("example");

    public static final ResourceLocation BOSS_DEFEATED_PACKET = ResourceLocation.fromNamespaceAndPath("boss_checklist", "boss_defeated");

    public static void registerPackets() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, BOSS_DEFEATED_PACKET, (buf, context) -> {

            String lineOfInformation = buf.readUtf();

            String bossId = lineOfInformation.split("#")[0];

            BossDataClientSaver.BossDefeated(bossId, true);
            BossDataClientSaver.defeatedBosses_InWorld.add(lineOfInformation);
            BossDataClientSaver.defeatedBossesIds_InWorld.add(bossId);
        });
    }
}
