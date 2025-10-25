package com.yori3o.boss_checklist.network;

import com.yori3o.boss_checklist.client.data.ClientDataSaver;

import net.minecraft.resources.ResourceLocation;

import dev.architectury.networking.NetworkManager;


public class BossDefeatedClientPacket {

    @SuppressWarnings("removal")
    public static void registerPackets() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ResourceLocation.fromNamespaceAndPath("boss_checklist", "boss_defeated"), (buf, context) -> {

            String lineOfInformation = buf.readUtf();

            ClientDataSaver.BossDefeatedInWorld(lineOfInformation);

        });
    }
}
