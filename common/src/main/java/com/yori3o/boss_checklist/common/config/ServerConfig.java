package com.yori3o.boss_checklist.common.config;


import java.nio.file.Path;

import com.yori3o.boss_checklist.impl.PlatformUtil;



public class ServerConfig extends JsonConfigManager<ServerConfig.Values> {

    public static class Values {
        public boolean saveBossKillerName = true;
        public boolean statisticsEnabled = true;
        public boolean asyncLogic = true;
    }

    private static final Path CONFIG_PATH = PlatformUtil.getConfigDir().resolve("boss_checklist_server.json");

    public ServerConfig() {
        super(Values.class, CONFIG_PATH);
    }

    @Override
    protected Values getDefaultConfig() {
        return new Values();
    }

}
