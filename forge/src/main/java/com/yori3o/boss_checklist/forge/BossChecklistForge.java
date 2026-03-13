package com.yori3o.boss_checklist.forge;


import com.yori3o.boss_checklist.common.event.EventHandler;
import com.yori3o.boss_checklist.common.BossChecklist;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;




@Mod(BossChecklist.MOD_ID)
public class BossChecklistForge {


    public BossChecklistForge() {
        (new BossChecklist()).init();

        MinecraftForge.EVENT_BUS.addListener(this::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingDamage);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerJoin);
    }


    private void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (!entity.level().isClientSide()) {
            EventHandler.whenEntityAllowDamage(entity, event.getSource(), event.getAmount()); 
        }
    }

    private void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        if (!entity.level().isClientSide()) {
            EventHandler.whenEntityDeath(entity, event.getSource());
        }
    }

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EventHandler.whenPlayerJoinToServer(player);
        }
    }
}