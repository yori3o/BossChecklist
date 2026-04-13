package com.yori3o.boss_checklist.common.config;


import com.yori3o.boss_checklist.common.BossChecklist;

import java.nio.file.Path;



public class ClientConfig extends JsonConfigManager<ClientConfig.Values> {

    public static class Values {
        public boolean progressionMode = false;
        public boolean progressionModePlus = false;
        public boolean animationsEnabled = true;
        public boolean showConfigScreen = true;
        public boolean openButtonEnabled = true;
        public boolean searchBarEnabled = false;
        public boolean progressBarEnabled = true;
        public boolean statisticsTabEnabled = true;
        public int openButtonYOffset = 0;
    }

    private static final Path CONFIG_PATH = BossChecklist.CONFIG_FOLDER.resolve("boss_checklist-client.json");

    public ClientConfig() {
        super(Values.class, CONFIG_PATH);
    }

    @Override
    protected Values getDefaultConfig() {
        return new Values();
    }
}
