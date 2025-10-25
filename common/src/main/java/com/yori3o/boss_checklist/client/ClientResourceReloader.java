package com.yori3o.boss_checklist.client;

import com.yori3o.boss_checklist.client.data.BossRegistry;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public class ClientResourceReloader implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(
        PreparationBarrier barrier, 
        ResourceManager resourceManager, 
        ProfilerFiller profilerA, 
        ProfilerFiller profilerB, 
        Executor backgroundExecutor, 
        Executor gameThreadExecutor
    ) {
        return CompletableFuture.runAsync(
            () -> this.doPrepareWork(resourceManager, profilerA), 
            backgroundExecutor
        )
        .thenCompose(barrier::wait) 
        .thenAcceptAsync(
            (preparedObject) -> this.doApplyWork(profilerB), 
            gameThreadExecutor
        );
    }

    private void doPrepareWork(ResourceManager resourceManager, ProfilerFiller profiler) { }

    private void doApplyWork(ProfilerFiller profiler) {
        BossRegistry.load();
    }
}