package com.yori3o.boss_checklist.common.event;


import com.yori3o.boss_checklist.common.BossChecklistClient;
import com.yori3o.boss_checklist.common.client.ClientGlobalStatistics;
import com.yori3o.boss_checklist.common.client.data.BossNameCache;
import com.yori3o.boss_checklist.common.client.data.BossProgressStorage;
import com.yori3o.boss_checklist.common.client.data.ClientDataSaver;
import com.yori3o.boss_checklist.common.client.gui.BossChecklistScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;



public class ClientEvents {


    public static boolean openChecklistKeyWasDown = false;


    protected static void updateClientBossDefeatedData() {
        ClientDataSaver.updateWorldKey();
        ClientDataSaver.load();
        BossNameCache.rebuild();
    }


    protected static void clearClientCache() {
        ClientGlobalStatistics.deleteStatsData();
        BossProgressStorage.clear();
        BossNameCache.invalidate();
    }


    protected static void checkKeybindPressed() {
        boolean down = BossChecklistClient.OPEN_CHECKLIST.isDown();
        if (down && !openChecklistKeyWasDown) {
            Minecraft.getInstance().gui.setScreen(new BossChecklistScreen(false));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }

        openChecklistKeyWasDown = down;
    }



}