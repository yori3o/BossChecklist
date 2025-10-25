package com.yori3o.boss_checklist.fabric;

/*import com.yori3o.boss_checklist.config.ServerConfig;*/
import com.yori3o.boss_checklist.BossChecklist;
import com.yori3o.boss_checklist.server.BossDefeated;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;


public class BossChecklistFabric implements ModInitializer {

    private static final BossDefeated BossDefeatedClass = new BossDefeated();

    @Override
    public void onInitialize() {
        BossChecklist BossChecklist2 = new BossChecklist();

        BossChecklist2.init();

        /*if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            BossChecklist.saveBossKiller = ServerConfig.saveBossKiller; }*/

        ServerLivingEntityEvents.AFTER_DEATH.register((LivingEntity entity, DamageSource source) -> {
            String killerName = "";

            if (source.getEntity() instanceof Player player) {
                killerName = player.getName().getString();
            }

            BossDefeatedClass.EntityKilled(entity, killerName);
        });
    }

}