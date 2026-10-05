package com.yori3o.boss_checklist.common.compat;


import com.yori3o.boss_checklist.impl.PlatformUtil;



public class Compats {

    
    public static boolean isCompletionistsIndexLoaded = false;


    public static void checkLoadedMods() {
        isCompletionistsIndexLoaded = PlatformUtil.isModLoaded("completionistsindex");
    }
}