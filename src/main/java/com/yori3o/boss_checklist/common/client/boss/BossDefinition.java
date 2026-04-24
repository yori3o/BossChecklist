package com.yori3o.boss_checklist.common.client.boss;


import com.yori3o.boss_checklist.impl.PlatformUtil;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is, one might say, read-only. It contains information from bosses.json.
 */
public class BossDefinition {

    
    private final String id;

    private List<String> drops;
    
    // objects instead of primitive types to set a default value when null
    private Integer scale;

    @SerializedName("y_offset")
    private int yOffset; // However, yOffset is 0 by default, so there is no need to set a default value for it - it is what it is by itself

    @SerializedName("broken_model") // the variable names are different from the JSON ones, so you need to specify them
    private boolean brokenModel = false;

    @SerializedName("rotate_y")
    private int rotateY;

    @SerializedName("additional_info")
    private boolean additionalInfo = false;

    private Float position;

    @SerializedName("wiki_link")
    private String wikiLink;

    private Integer health;
    private Integer armor;

    private Integer type; // 1-boss, 2-miniboss

    @SerializedName("mod_version")
    private String modVersion;

    public boolean replace = false;


    public BossDefinition(String id) {
        this.id = id;
    }

    public BossDefinition(String id, float position, int scale, int yOffset, boolean brokenModel, boolean miniboss, List<String> drops, String version, String wikiLink, boolean additionalInfo) {
        this.id = id;
        this.position = position;
        this.scale = scale;
        this.yOffset = yOffset;
        this.brokenModel = brokenModel;
        if (miniboss) this.type = 2;
        this.drops = drops;
        this.modVersion = version;
        this.additionalInfo = additionalInfo;
        this.wikiLink = wikiLink;
        this.applyDefaults();
    }

    public String id() {
        return id;
    }

    public String modId() {
        return id.split(":")[0];
    }

    public List<String> drops() {
        return drops;
    }

    public int scale() {
        return scale;
    }

    public int yOffset() {
        return yOffset;
    }

    public int rotateY() {
        return rotateY;
    }
    public boolean brokenModel() {
        return brokenModel;
    }

    public boolean additionalInfo() {
        return additionalInfo;
    }

    public float position() {
        return position;
    }

    public String wikiLink() {
        if (wikiLink == null) return "";
        return wikiLink;
    }

    public int health() {
        return health;
    }

    public int armor() {
        return armor;
    }

    public int type() {
        return type;
    }

    public String modVersion() {
        if (modVersion == null) return "";
        return modVersion;
    }

    public void applyDefaults() {
        if (drops == null) drops = new ArrayList<>();
        if (scale == null) scale = 10;
        if (health == null) health = -1;
        if (armor == null) armor = -1;
        if (position == null) position = 999f;
        if (type == null) type = 1;
    }

    public void applyPostLoadFixes() {
        // here you can add unique compatibility, for example, mods that change vanilla bosses

        if (id.equals("minecraft:wither")) {
            if (PlatformUtil.isModLoaded("witherreincarnated")) {
                scale -= 1;
            }
        } else if (id.equals("minecraft:ender_dragon")) {
            if (PlatformUtil.isModLoaded("mr_limesplatus_ending") || PlatformUtil.isModLoaded("mr_true_ending")) {
                health = 300;
            }
        }
    }

    public void setPosition(float position) {
        this.position = position;
    }
}
