package com.yori3o.boss_checklist.neoforge;


import com.yori3o.boss_checklist.common.event.EventHandler;
import com.yori3o.boss_checklist.common.BossChecklist;

import net.minecraft.world.entity.LivingEntity;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;



@Mod(BossChecklist.MOD_ID)
public class BossChecklistNeoForge {


    public BossChecklistNeoForge() {
        (new BossChecklist()).init();

        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(this::onLivingDamage);
    }


    private void onLivingDamage(LivingIncomingDamageEvent event) {
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
}