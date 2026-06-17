package com.yori3o.boss_checklist.impl;


import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.util.function.Consumer;



public class PlatformCommandRegistry {


    public static void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            consumer.accept(dispatcher);
        });
    }
   
}

