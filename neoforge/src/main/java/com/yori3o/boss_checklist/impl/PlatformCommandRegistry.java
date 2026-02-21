package com.yori3o.boss_checklist.impl;


import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.function.Consumer;



public class PlatformCommandRegistry {


    public static void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            consumer.accept(event.getDispatcher());
        });
    }
  
}

