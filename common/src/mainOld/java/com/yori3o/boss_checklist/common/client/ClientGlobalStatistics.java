package com.yori3o.boss_checklist.common.client;


/**
 * This class contains the top 3 players in damage from the entire server.
 */
public final class ClientGlobalStatistics {


    public static String top1;
    public static String top2;
    public static String top3;

    public static String damage1;
    public static String damage2;
    public static String damage3;

    
    public static void setTop3(String[] list) {
        if (!list[0].equals("")) {
            top1 = list[0];
            damage1 = list[1];
            if (!list[2].equals("")) {
                top2 = list[2];
                damage2 = list[3];
                if (!list[4].equals("")) {
                    top3 = list[4];
                    damage3 = list[5];
                }
            }
        }
    }

    public static void deleteStatsData() {
        top1 = null;
        top2 = null;
        top3 = null;

        damage1 = null;
        damage2 = null;
        damage3 = null;
    }
}