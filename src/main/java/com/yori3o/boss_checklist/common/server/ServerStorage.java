package com.yori3o.boss_checklist.common.server;


import com.yori3o.boss_checklist.common.server.data.ServerBossAttempt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class stores the entire server cache: killed bosses, their killers, recent battles with them, damage statistics.
 */
public final class ServerStorage {
    
    
    public static Map<String, String> defeatedBossesAndTheirKillers = new HashMap<>();
    
    public static Map<String, ServerBossAttempt> serverBossAttempts = new HashMap<>();
    
    public static Map<String, Float> playerDamages = new HashMap<>();

    public static boolean needsSaving = false;



    public static void markBoss(String id, String killerName, ServerBossAttempt serverBossAttempt, boolean defeated) {
        needsSaving = true;
        if (defeated) {
            defeatedBossesAndTheirKillers.put(id, killerName);
            serverBossAttempts.put(id, serverBossAttempt);
        } else {
            defeatedBossesAndTheirKillers.remove(id);
            serverBossAttempts.remove(id);
        }
    }


    public static void addPlayerDamageGlobal(String playerName, float damage) {
        needsSaving = true;
        playerDamages.merge(playerName, damage, Float::sum);
    }

    
    // It's a terrible crutch, but I don't want to redo it, because "if it works, don't touch it."
    public static String getTop3PlayersNamesAndDamagesGlobal_SplittedByHashtag() {

        List<Map.Entry<String, Float>> entryList = new ArrayList<>(playerDamages.entrySet());

        entryList.sort(Map.Entry.<String, Float>comparingByValue().reversed());

        String result;

        if (entryList.size() > 0) { 
            result = entryList.get(0).getKey() + "#" + (Math.round(entryList.get(0).getValue() * 10) / 10f); // top 1

            if (entryList.size() > 1) {
                result = result + "#" + entryList.get(1).getKey() + "#" + (Math.round(entryList.get(1).getValue() * 10) / 10f); // top 2

                if (entryList.size() > 2) {
                    result = result + "#" + entryList.get(2).getKey() + "#"  + (Math.round(entryList.get(2).getValue() * 10) / 10f); // top 3
                } else {
                    result = result + "##";
                }

            } else {
                result = result + "####";
            }

        } else { // no one
            result = "#####";
        }
        
        return result;
    }
    
}