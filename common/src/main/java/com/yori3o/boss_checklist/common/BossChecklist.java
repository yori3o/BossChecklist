package com.yori3o.boss_checklist.common;


import com.yori3o.boss_checklist.common.command.SetDefeatedCommand;
import com.yori3o.boss_checklist.impl.PlatformUtil;



public class BossChecklist {

    
    public static final String MOD_ID = "boss_checklist";


    public void init() {

        SetDefeatedCommand.register();

        // --- only client logic ---
        if (PlatformUtil.isClient()) {
            BossChecklistClient.initClient();
        }
    }
}