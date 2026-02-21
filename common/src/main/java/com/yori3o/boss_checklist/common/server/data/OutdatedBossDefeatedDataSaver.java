package com.yori3o.boss_checklist.common.server.data;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.core.HolderLookup;

import java.util.HashSet;
import java.util.Set;


/**
 * This class reads the data that was written in older versions of the mod (3.4.0-)
 */
public class OutdatedBossDefeatedDataSaver extends SavedData {
    
    private static final String DATA_NAME = "boss_checklist_defeated_data";
    private final Set<String> defeatedBosses = new HashSet<>();


    public Set<String> getDefeatedBosses() {
        return defeatedBosses;
    }


    public OutdatedBossDefeatedDataSaver(HolderLookup.Provider provider) {}

    public OutdatedBossDefeatedDataSaver() {}


    public static OutdatedBossDefeatedDataSaver load(CompoundTag tag, HolderLookup.Provider provider) {
        OutdatedBossDefeatedDataSaver data = new OutdatedBossDefeatedDataSaver();
        ListTag bossList = tag.getList("DefeatedBosses", 8); 
        
        for (int i = 0; i < bossList.size(); i++) {
            data.defeatedBosses.add(bossList.getString(i));
        }
        return data;
    }

    @Override
    // it can't write anymore
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        /*ListTag bossList = new ListTag();
        for (String bossId : defeatedBosses) {
            bossList.add(net.minecraft.nbt.StringTag.valueOf(bossId.toString()));
        }
        tag.put("DefeatedBosses", bossList);*/
        return null;
    }

    public static OutdatedBossDefeatedDataSaver get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        
        // FOR 1.21.1
        SavedData.Factory<OutdatedBossDefeatedDataSaver> factory = new SavedData.Factory<>(
            OutdatedBossDefeatedDataSaver::new, 
            OutdatedBossDefeatedDataSaver::load, 
            null 
        );
        return storage.computeIfAbsent(
            factory, 
            DATA_NAME
        );

        // FOR 1.20.1
        /*return storage.computeIfAbsent(
            BossDefeatedDataSaver::load, BossDefeatedDataSaver::new, DATA_NAME
        );*/
    }
}