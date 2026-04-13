package com.yori3o.boss_checklist.common.mixin;


import com.yori3o.boss_checklist.common.client.data.OverlapManager;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.ResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.HashMap;



@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {


    @Shadow 
    private Map<String, String> storage;



    @Inject(
        method = "loadFrom",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void injectCustomLang(ResourceManager resourceManager,
                                         List<String> languageStack,
                                         boolean defaultRightToLeft,
                                         CallbackInfoReturnable<ClientLanguage> cir) {

        ClientLanguage original = cir.getReturnValue();

        Map<String, String> map = new HashMap<>(original.storage);

        if (!OverlapManager.OVERLAP_EN_US.isEmpty()) {
            if (!map.containsKey(OverlapManager.OVERLAP_EN_US.keySet().toArray()[0])) {
                map.putAll(OverlapManager.OVERLAP_EN_US);
            }
        }
        
        cir.setReturnValue(new ClientLanguage(map, original.isDefaultRightToLeft()));
    }
}