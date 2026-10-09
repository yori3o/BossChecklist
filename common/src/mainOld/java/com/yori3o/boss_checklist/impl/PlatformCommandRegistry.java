package com.yori3o.boss_checklist.impl;


import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;



public class PlatformCommandRegistry {

    public static void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        throw new RuntimeException("Platform-specific implementation missing");
    }
    
}

