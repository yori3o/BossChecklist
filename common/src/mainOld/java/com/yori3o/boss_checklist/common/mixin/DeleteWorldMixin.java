package com.yori3o.boss_checklist.common.mixin;


import com.yori3o.boss_checklist.common.client.data.ClientDataSaver;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.impl.PlatformUtil;

import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;


/**
 * This mixin removes the corresponding file from ".boss_checklist_data" when deleting a world.
 */
@Mixin(LevelStorageAccess.class)
public class DeleteWorldMixin {

    
    @Inject(
        method = "deleteLevel",
        at = @At("HEAD"),
        cancellable = false
    )
    private void onDeleteLevel(CallbackInfo ci) {

        String worldName = ((LevelStorageSource.LevelStorageAccess)(Object)this).getLevelId();

        Path dataFile = new File(
            PlatformUtil.getGameDir().toString(),
            ClientDataSaver.DATA_FOLDER_NAME + File.separator + "singleplayer_" + worldName + File.separator + ClientDataSaver.JSON_FILE_NAME
        ).toPath();
        
        Path dataFolder = new File(
            PlatformUtil.getGameDir().toString(),
            ClientDataSaver.DATA_FOLDER_NAME + File.separator + "singleplayer_" + worldName
        ).toPath();

        if (Files.exists(dataFolder)) {
            if (Files.exists(dataFile)) {
                try {
                    Files.delete(dataFile);
                } catch (IOException e) {
                    LoggerUtil.error("Failed to delete file: " + dataFolder);
                    e.printStackTrace();
                }
            }
            try {
                Files.delete(dataFolder);
            } catch (IOException e) {
                LoggerUtil.error("Failed to delete folder: " + dataFolder);
                e.printStackTrace();
            }
        }
    }
}