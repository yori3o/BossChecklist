package com.yori3o.boss_checklist.client.data;

import java.util.ArrayList;
import java.util.List;

import com.yori3o.boss_checklist.utils.LoggerUtil;


public class BossData {
    private String id;
    private List<String> drops;
    private int scale;
    private int y_offset;
    private Boolean broken_model;
    private Boolean additional_info;
    private float position;
    private String wiki_link;
    private float health;
    private float armor;

    public Boolean replace; // it for merge json's
    private Boolean is_defeated;

    public BossData(String id, List<String> drops, int scale, int y_offset, Boolean broken_model, Boolean additional_info, float position, String wiki_link, float health, float armor, Boolean isDefeated, Boolean replace) {
        this.id = id;
        this.drops = drops;
        this.scale = scale;
        this.y_offset = y_offset;
        this.broken_model = broken_model;
        this.additional_info = additional_info;
        this.position = position;
        this.wiki_link = wiki_link;
        this.health = health;
        this.armor = armor;
        
        this.is_defeated = isDefeated;
        this.replace = replace;
    }

    public void applyDefaults() {
        drops = (id == null) ? new ArrayList<>() : drops;
        scale = (scale == 0) ? 10 : scale;
        y_offset = (y_offset == 0) ? 0 : y_offset;
        broken_model = (broken_model == null) ? false : broken_model;
        additional_info = (additional_info == null) ? false : additional_info;
        position = (position == 0) ? 999 : position;
        wiki_link = (wiki_link == null) ? "" : wiki_link;
        health = (health == 0) ? -1 : health;
        armor = (armor == 0) ? -1 : armor;
    }

    public void applyReplaceDefault() {
        LoggerUtil.LOGGER.info(id +" is replase is " + replace);
        replace = (replace == null) ? false : replace;
        LoggerUtil.LOGGER.info(id +" after defaulting is replase is " + replace);
    }

    public String getId() { return id; }
    public String getModId() { return id.split(":")[0]; }
    public List<String> getDrops() { return drops; }
    public int getScale() { return scale; }
    public int getYOffset() { return y_offset; }
    public float getPosition() { return position; }
    public String getWikiLink() { return wiki_link; }
    public float getHealth() { return health; }
    public float getArmor() { return armor; }
    public Boolean isDefeated() { return is_defeated; }
    public Boolean isBrokenModel() { return broken_model; }
    public Boolean isAdditionalInfo() { return additional_info; }

    public void SetDefeated(Boolean bool) { is_defeated = bool; }
}