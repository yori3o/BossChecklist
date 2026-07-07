package com.yori3o.boss_checklist.forge;


import com.yori3o.boss_checklist.common.event.EventHandler;
import com.yori3o.boss_checklist.common.BossChecklist;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;




@EventBusSubscriber(modid = BossChecklist.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BossChecklistForgeServerEvents {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        EventHandler.whenServerStarted(event.getServer());
    }

    /*@SubscribeEvent
    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        EventHandler.whenRegisterPayloads();
    }*/

}