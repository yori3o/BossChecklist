package com.yori3o.boss_checklist.config;

import java.nio.file.Path;
import dev.architectury.platform.Platform;

public class ServerConfig extends JsonConfigManager<ServerConfig.Values> {

    public static class Values {
        public boolean SaveBossKiller = true;
    }

    private static final Path CONFIG_PATH = Platform.getConfigFolder().resolve("boss_checklist_server.json");

    public ServerConfig() {
        super(Values.class, CONFIG_PATH);
    }

    @Override
    protected Values getDefaultConfig() {
        return new Values();
    }

    public boolean isSaveBossKiller() { return get().SaveBossKiller; }
    public void setSaveBossKiller(boolean value) { get().SaveBossKiller = value; save(); }
}
