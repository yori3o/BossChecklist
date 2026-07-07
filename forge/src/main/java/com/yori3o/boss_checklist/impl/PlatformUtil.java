package com.yori3o.boss_checklist.impl;



import java.nio.file.Path;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;



public class PlatformUtil {


    public static boolean isModLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    public static boolean isClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    public static String getVerison(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse(null);
    }

    public static String getModName(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(null);
    }

    public static boolean isFabric() {
        return false;
    }
    
}
