package com.yori3o.boss_checklist.neoforge;

import com.yori3o.boss_checklist.BossChecklist;
import com.yori3o.boss_checklist.data.BossRegistry;
import com.yori3o.boss_checklist.server.BossDefeatedAndLoad;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.fml.ModLoadingContext;


@Mod(BossChecklist.MOD_ID)
public class BossChecklistNeoForge {

    private static final BossDefeatedAndLoad BossDefeatedClass = new BossDefeatedAndLoad();

    //@Mod.EventBusSubscriber
    public BossChecklistNeoForge() {
        BossChecklist BossChecklist = new BossChecklist();
        
        BossChecklist.init();

        IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        modEventBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        BossRegistry.load();
    }

    private void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        if (!entity.level().isClientSide()) {
            Entity source = event.getSource().getEntity();
            String killerName = "";

            if (source instanceof Player player) {
                killerName = player.getName().getString();
            }

            BossDefeatedClass.EntityKilled(entity, killerName);
        }
    }

}