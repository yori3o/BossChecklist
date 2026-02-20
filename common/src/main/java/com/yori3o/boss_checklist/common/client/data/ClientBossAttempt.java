package com.yori3o.boss_checklist.common.client.data;


import java.util.LinkedHashMap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * This class is used to store information about the last boss fight.
*/
public class ClientBossAttempt {


    public final String id;
    private final String startTime;
    private final String endTime;
    public LinkedHashMap<String, String> damageMap_top3 = new LinkedHashMap<>();
    public final String duration;
    public final String dateAndTime;



    public ClientBossAttempt(String id, String startTime, String endTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = getDuration();
        this.dateAndTime = getDateAndTime();
    }


    public void addPlayerDamagesTop3(String top3) {
        String[] namesAndDamages = top3.split("#", -1);
        if (!namesAndDamages[0].equals("")) {
            damageMap_top3.put(namesAndDamages[0], namesAndDamages[1]);
            if (!namesAndDamages[2].equals("")) {
                damageMap_top3.put(namesAndDamages[2], namesAndDamages[3]);
                if (!namesAndDamages[4].equals("")) {
                    damageMap_top3.put(namesAndDamages[4], namesAndDamages[5]);
                }
            }
        }
    }



    private String getDateAndTime() {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH.mm.ss");

        LocalDateTime localDateTime = LocalDateTime.parse(startTime, inputFormatter);

        ZoneId fromZone = ZoneId.of("UTC");
        ZonedDateTime zonedDateTimeUTC = localDateTime.atZone(fromZone);

        ZoneId toZone = ZoneId.systemDefault();
        ZonedDateTime zonedDateTimeSystem = zonedDateTimeUTC.withZoneSameInstant(toZone);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd - HH:mm:ss");

        String adjustedDate = zonedDateTimeSystem.format(formatter);

        return adjustedDate;
    }
    

    
    public String getDuration() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH.mm.ss");

        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        Duration duration = Duration.between(start, end);

        int totalSeconds = (int) Math.abs(duration.toSeconds());

        if (totalSeconds < 1) return "<1s";

        if (totalSeconds > 86399) return ">1 day";

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return seconds + "s";
        }
    }
}
