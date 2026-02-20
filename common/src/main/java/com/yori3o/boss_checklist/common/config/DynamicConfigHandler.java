package com.yori3o.boss_checklist.common.config;



/**
 * This class loads and writes the config, and is also needed for the config menu to work.
 * The config values ​​need to be taken from here.
 */
public class DynamicConfigHandler {

    // --- server config variables ---
    public static boolean saveBossKiller_dynamic;
    public static boolean statisticsEnabled_dynamic;
    public static boolean asyncLogic_dynamic;

    // --- client config variables ---
    public static boolean progressionMode_dynamic;
    public static boolean progressionModePlus_dynamic;
    public static boolean animationsEnabled;
    public static boolean openButtonEnabled_dynamic;
    public static boolean showConfigScreen;
    public static boolean searchBarEnabled_dynamic;
    public static boolean progressBarEnabled;
    public static boolean statisticsBarEnabled;



    public static void ClientConfigLoad() {
        ClientConfig cc = new ClientConfig();
        cc.load();
        animationsEnabled = cc.get().animationsEnabled;
        showConfigScreen = cc.get().showConfigScreen;
        progressBarEnabled = cc.get().progressBarEnabled;
        statisticsBarEnabled = cc.get().statisticsBarEnabled;
        ClientConfigUpdate(cc.get());
    }

    public static void ServerConfigLoad() {
        ServerConfig sc = new ServerConfig();
        sc.load();
        ServerConfigUpdate(sc.get());
    }


    public static void ClientConfigUpdate(ClientConfig.Values values) {
        progressionMode_dynamic = values.progressionMode;
        progressionModePlus_dynamic = values.progressionModePlus;
        openButtonEnabled_dynamic = values.openButtonEnabled;
        searchBarEnabled_dynamic = values.searchBarEnabled;
    }

    public static void ServerConfigUpdate(ServerConfig.Values values) {
        saveBossKiller_dynamic = values.saveBossKiller;
        statisticsEnabled_dynamic = values.statisticsEnabled;
        asyncLogic_dynamic = values.asyncLogic;
    }

}
