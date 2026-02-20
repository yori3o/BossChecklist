package com.yori3o.boss_checklist.common.server.data;


import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class stores information about the last boss battle.
 */
public class ServerBossAttempt {

    public final String id;
    public final String uuid;

    public String startTime = "";
    public String endTime = "";

    public Map<String, Float> damageMap = new HashMap<>();



    public ServerBossAttempt(String id, String uuid) {
        this.id = id;
        this.uuid = uuid;
    }



    
    public void saveFormattedTime(Instant time, boolean isStart) {
        ZonedDateTime utc = time.atZone(ZoneOffset.UTC);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH.mm.ss");

        String formatted = utc.format(formatter);

        if (isStart) {
            this.startTime = formatted;
        } else {
            this.endTime = formatted;
        }
    }



    public void addPlayerDamage(String playerName, float damage) {
        damageMap.merge(playerName, damage, Float::sum);
    }




    public String getTop3PlayersNamesAndDamages_SplittedByHashtag() {

        // 1. Создаем список из всех записей (имя-урон) в карте
        List<Map.Entry<String, Float>> entryList = new ArrayList<>(damageMap.entrySet());

        // 2. Сортируем список по значениям (урону) в порядке убывания (от большего к меньшему)
        entryList.sort(Map.Entry.<String, Float>comparingByValue().reversed());


        String result;

        if (entryList.size() > 0) { 
            result = entryList.get(0).getKey() + "#" + (Math.round(entryList.get(0).getValue() * 10) / 10f); // топ 1

            if (entryList.size() > 1) {
                result = result + "#" + entryList.get(1).getKey() + "#" + (Math.round(entryList.get(1).getValue() * 10) / 10f); // топ 2

                if (entryList.size() > 2) {
                    result = result + "#" + entryList.get(2).getKey() + "#"  + (Math.round(entryList.get(2).getValue() * 10) / 10f); // топ 3
                } else {
                    result = result + "##";
                }

            } else {
                result = result + "####";
            }

        } else { // никого нет
            result = "#####";
        }
        
        return result;
    }
}
