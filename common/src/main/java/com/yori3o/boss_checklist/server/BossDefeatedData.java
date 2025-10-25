package com.yori3o.boss_checklist.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.core.HolderLookup;
import java.util.HashSet;
import java.util.Set;


public class BossDefeatedData extends SavedData {
    
    private static final String DATA_NAME = "boss_checklist_defeated_data";
    private final Set<String> defeatedBosses = new HashSet<>();

    public Set<String> getDefeatedBosses() {
        return defeatedBosses;
    }

    public void addBoss(String lineOfInformation) {
        if (defeatedBosses.add(lineOfInformation)) { 
            this.setDirty(); 
        }
    }

    public void removeBoss(String lineOfInformation) {

        Set<String> defeatedBossesCopy = new HashSet<>();

        for (String boss : defeatedBosses) {
            String bossId = boss.split("#")[0];

            if (!bossId.equals(lineOfInformation.split("#")[0])) {
                defeatedBossesCopy.add(boss);  
            }
        }
        defeatedBosses.clear();
        defeatedBosses.addAll(defeatedBossesCopy);

        this.setDirty(); 
    }

    public BossDefeatedData(HolderLookup.Provider provider) {}

    public BossDefeatedData() {}
    

    public static BossDefeatedData load(CompoundTag tag, HolderLookup.Provider provider) {
        BossDefeatedData data = new BossDefeatedData();
        ListTag bossList = tag.getList("DefeatedBosses", 8); 
        
        for (int i = 0; i < bossList.size(); i++) {
            data.defeatedBosses.add(bossList.getString(i));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag bossList = new ListTag();
        for (String bossId : defeatedBosses) {
            bossList.add(net.minecraft.nbt.StringTag.valueOf(bossId.toString()));
        }
        tag.put("DefeatedBosses", bossList);
        return tag;
    }

    public static BossDefeatedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        
        // FOR 1.21.1
        SavedData.Factory<BossDefeatedData> factory = new SavedData.Factory<>(
            BossDefeatedData::new, 
            BossDefeatedData::load, 
            null 
        );
        // FOR 1.21.1
        return storage.computeIfAbsent(
            factory, 
            DATA_NAME
        );

        // FOR 1.20.1
        /*return storage.computeIfAbsent(
            BossDefeatedData::load, BossDefeatedData::new, "boss_checklist_defeated_data"
        );*/
    }
}