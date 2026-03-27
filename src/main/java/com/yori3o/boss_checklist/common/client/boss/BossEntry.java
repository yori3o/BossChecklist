package com.yori3o.boss_checklist.common.client.boss;


/**
 * This class connects all the information about the boss for later use everywhere.
 */
public final class BossEntry {


    private final BossDefinition definition;
    private final BossProgress progress;

    
    public BossEntry(BossDefinition definition, BossProgress progress) {
        this.definition = definition;
        this.progress = progress;
    }

    public BossDefinition definition() {
        return definition;
    }

    public BossProgress progress() {
        return progress;
    }

    public String id() {
        return definition.id();
    }
}
