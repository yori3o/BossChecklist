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

    // 4. МЕТОД ПОЛУЧЕНИЯ ДАННЫХ (ИСПОЛЬЗУЕТ НОВЫЙ API)
    public static BossDefeatedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        
        // СОЗДАНИЕ FACTORY: Объединяем конструктор и метод загрузки
        SavedData.Factory<BossDefeatedData> factory = new SavedData.Factory<>(
            // 1. Новый конструктор, принимающий Provider
            BossDefeatedData::new, 
            // 2. Новый метод загрузки, принимающий Provider
            BossDefeatedData::load, 
            // 3. DataFixTypes (null, если не используется DataFixer)
            null 
        );

        // ИСПОЛЬЗОВАНИЕ computeIfAbsent: Новый метод для получения/создания данных
        return storage.computeIfAbsent(
            factory, 
            DATA_NAME
        );
    }
}