package com.yori3o.boss_checklist.common.config;


import java.nio.file.Path;

import com.yori3o.boss_checklist.common.BossChecklist;



public class ServerConfig extends JsonConfigManager<ServerConfig.Values> {

    public static class Values {
        public boolean saveBossKillerName = true;
        public boolean statisticsEnabled = true;
        public boolean asyncLogic = true;
    }

    private static final Path CONFIG_PATH = BossChecklist.CONFIG_FOLDER.resolve("boss_checklist-server.json");

    public ServerConfig() {
        super(Values.class, CONFIG_PATH);
    }

    @Override
    protected Values getDefaultConfig() {
        return new Values();
    }

}
