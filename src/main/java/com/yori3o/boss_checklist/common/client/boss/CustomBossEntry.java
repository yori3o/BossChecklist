package com.yori3o.boss_checklist.common.client.boss;



public class CustomBossEntry extends BossEntry {


    private String name;
    private String modName;
    private String spawnInfo;


    public CustomBossEntry(BossDefinition definition, BossProgress progress, String name, String modName, String spawnInfo) {
        super (definition, progress);
        this.name = name;
        this.modName = modName;
        this.spawnInfo = spawnInfo;
    }

    public String name() {
        return name;
    }

    public String modName() {
        return modName;
    }

    public String spawnInfo() {
        return spawnInfo;
    }
}
