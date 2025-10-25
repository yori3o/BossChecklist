package com.yori3o.boss_checklist.config;

import java.nio.file.Path;
import dev.architectury.platform.Platform;

public class ClientConfig extends JsonConfigManager<ClientConfig.Values> {

    public static class Values {
        public boolean ProgressionMode = false;
    }

    private static final Path CONFIG_PATH = Platform.getConfigFolder().resolve("boss_checklist_client.json");

    public ClientConfig() {
        super(Values.class, CONFIG_PATH);
    }

    @Override
    protected Values getDefaultConfig() {
        return new Values();
    }

    public boolean isProgressionMode() { return get().ProgressionMode; }
    public void setProgressionMode(boolean value) { get().ProgressionMode = value; save(); }
}
