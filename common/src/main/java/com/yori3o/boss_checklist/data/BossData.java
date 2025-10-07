package com.yori3o.boss_checklist.data;

import java.util.List;

public class BossData {
    private final String id;
    private final String mod_id;
    private final List<String> drops;
    private final int scale;
    private final int y_offset;
    private final int position;
    private Boolean is_defeated;

    public BossData(String id, String modId, List<String> drops, int scale, int y_offset, int position, Boolean isDefeated) {
        this.id = id;
        this.mod_id = modId;
        this.drops = drops;
        this.scale = scale;
        this.y_offset = y_offset;
        this.position = position;
        this.is_defeated = isDefeated;
    }

    public String getId() { return id; }
    public String getModId() { return mod_id; }
    public List<String> getDrops() { return drops; }
    public int getScale() { return scale; }
    public int getYOffset() { return y_offset; }
    public int getPosition() { return position; }
    public Boolean isDefeated() { return is_defeated; }

    public void SetDefeated(Boolean bool) { is_defeated = bool; }
}