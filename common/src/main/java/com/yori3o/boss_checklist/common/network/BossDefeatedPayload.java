package com.yori3o.boss_checklist.common.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;



public record BossDefeatedPayload(String bossId, String killer, boolean defeated, boolean fresh,
                    String startTime, String endTime, String attemptTop3, String globalTop3) implements CustomPacketPayload {

    public static final Type<BossDefeatedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("boss_checklist", "boss_defeated"));

    public static final StreamCodec<FriendlyByteBuf, BossDefeatedPayload> CODEC =
        StreamCodec.of(
                (buf, payload) -> {
                    buf.writeUtf(payload.bossId());
                    buf.writeUtf(payload.killer());
                    buf.writeBoolean(payload.defeated());
                    buf.writeBoolean(payload.fresh());
                    buf.writeUtf(payload.startTime());
                    buf.writeUtf(payload.endTime());
                    buf.writeUtf(payload.attemptTop3());
                    buf.writeUtf(payload.globalTop3());
                },
                buf -> new BossDefeatedPayload(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf()
                )
        );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
