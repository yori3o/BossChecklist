package com.yori3o.boss_checklist.impl;


import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.function.Consumer;



public class PlatformCommandRegistry {


    public static void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            consumer.accept(event.getDispatcher());
        });
    }
  
}

