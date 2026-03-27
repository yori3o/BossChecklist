package com.yori3o.boss_checklist.common.mixin;


import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.server.ServerStorage;
import com.yori3o.boss_checklist.common.server.data.BossChecklistJsonDataSaver;
import com.yori3o.boss_checklist.common.util.LoggerUtil;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.storage.LevelResource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.io.File;
import java.util.concurrent.CompletableFuture;


/**
 * This mixin saves all server data when Minecraft saves chunks, entities, etc.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {


    @Inject(
        method = "save", 
        at = @At(value = "TAIL")
    )
    private void saveBossChecklistData(ProgressListener progressListener, boolean bl, boolean bl2, CallbackInfo ci) {
        if (!bl2) {
            File worldDir = ((ServerLevel)(Object)this).getServer().getWorldPath(LevelResource.ROOT).toFile();
            if (DynamicConfigHandler.server().asyncLogic) {
                CompletableFuture.runAsync(() -> {
                    saveData(worldDir);
                });
            } else {
                saveData(worldDir);
            }

        }
    }

    private void saveData(File worldDir) {
        try {
            BossChecklistJsonDataSaver.saveDefeatedBosses(worldDir, ServerStorage.defeatedBossesAndTheirKillers);
            if (DynamicConfigHandler.server().statisticsEnabled) {
                BossChecklistJsonDataSaver.saveLatestBossBattles(worldDir, ServerStorage.serverBossAttempts);
                BossChecklistJsonDataSaver.saveGlobalStatistics(worldDir, ServerStorage.playerDamages);
            }
        } catch (Exception e) {
            LoggerUtil.LOGGER.error("Unexpected error while saving data to world folder: ", e);
        }
        
    }

}

