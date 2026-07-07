package com.yori3o.boss_checklist.common.mixin;


import net.minecraft.client.resources.language.ClientLanguage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;



@Mixin(ClientLanguage.class)
public interface ClientLanguageAccessor {

    @Accessor("storage")
    Map<String, String> getStorage();

    @Accessor("storage")
    @Mutable
    void setStorage(Map<String, String> value);
}