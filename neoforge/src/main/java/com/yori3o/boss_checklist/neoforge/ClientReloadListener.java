package com.yori3o.boss_checklist.neoforge;


import com.yori3o.boss_checklist.common.client.data.BossRegistry;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;



public class ClientReloadListener implements PreparableReloadListener {


    @Override
    public CompletableFuture<Void> reload(
        PreparationBarrier barrier, 
        ResourceManager resourceManager, 
        ProfilerFiller profilerA, // FOR 1.21.1-
        ProfilerFiller profilerB, // FOR 1.21.1-
        Executor backgroundExecutor, 
        Executor gameThreadExecutor
    ) {
        return CompletableFuture.runAsync(
            () -> {}, 
            backgroundExecutor
        )
        .thenCompose(barrier::wait) 
        .thenAcceptAsync(
            (preparedObject) -> this.doApplyWork(resourceManager), 
            gameThreadExecutor
        );
    }
    
    private void doApplyWork(ResourceManager resourceManager) {
        BossRegistry.reload();
    }
}