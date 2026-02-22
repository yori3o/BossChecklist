package com.yori3o.boss_checklist.common.config;


/**
 * This class stores instances of configs from which you can take and write values, as well as load and save them to disk.
 */
public class DynamicConfigHandler {
    

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
    }

    public static void loadServer() {
        sc.load();
    }
}
