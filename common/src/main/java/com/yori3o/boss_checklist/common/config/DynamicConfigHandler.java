package com.yori3o.boss_checklist.common.config;


/**
 * This class stores instances of configs from which you can take and write values, as well as load and save them to disk.
 */
public class DynamicConfigHandler {

    // --- server config variables ---
    /*public static boolean saveBossKillerName_dynamic;
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
    public static boolean statisticsTabEnabled;
    public static int openButtonYOffset;*/

    public static ClientConfig cc = new ClientConfig();
    public static ServerConfig sc = new ServerConfig();


    public static ClientConfig.Values client() {
        return cc.get();
    }

    public static ServerConfig.Values server() {
        return sc.get();
    }

    public static void loadClient() {
        cc.load();
        /*animationsEnabled = cc.get().animationsEnabled;
        showConfigScreen = cc.get().showConfigScreen;
        progressBarEnabled = cc.get().progressBarEnabled;
        statisticsTabEnabled = cc.get().statisticsTabEnabled;
        openButtonYOffset = cc.get().openButtonYOffset;
        ClientConfigUpdate(cc.get());*/
    }

    public static void loadServer() {
        sc.load();
        //ServerConfigUpdate(sc.get());
    }


    /*public static void ClientConfigUpdate(ClientConfig.Values values) {
        progressionMode_dynamic = values.progressionMode;
        progressionModePlus_dynamic = values.progressionModePlus;
        openButtonEnabled_dynamic = values.openButtonEnabled;
        searchBarEnabled_dynamic = values.searchBarEnabled;
        //cc.save();
    }

    public static void ServerConfigUpdate(ServerConfig.Values values) {
        saveBossKillerName_dynamic = values.saveBossKillerName;
        statisticsEnabled_dynamic = values.statisticsEnabled;
        asyncLogic_dynamic = values.asyncLogic;
        //sc.save();
    }*/

}
