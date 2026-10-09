package com.yori3o.boss_checklist.common.command;


import com.yori3o.boss_checklist.common.client.boss.BossDefinition;
import com.yori3o.boss_checklist.common.client.data.BossRegistry;
import com.yori3o.boss_checklist.common.network.ServerSender;
import com.yori3o.boss_checklist.common.server.ServerStorage;
import com.yori3o.boss_checklist.common.server.data.ServerBossIdsLoader;
import com.yori3o.boss_checklist.impl.PlatformCommandRegistry;

import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;



public final class SetDefeatedCommand {

    public static void register() {
        PlatformCommandRegistry.registerCommand((dispatcher) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("boss_checklist")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("set_defeated")
                    .then(Commands.argument("boss_id", ResourceLocationArgument.id())
                        .suggests(SetDefeatedCommand::suggestBossIds)
                        .then(Commands.argument("defeated", BoolArgumentType.bool())
                            .executes(SetDefeatedCommand::executeSetDefeated)
                        )
                    )
                )
        );
    }

    private static CompletableFuture<Suggestions> suggestBossIds(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (BossDefinition boss : BossRegistry.all() ) {
            builder.suggest(boss.id());
        }
        return builder.buildFuture();
    }

    private static int executeSetDefeated(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String bossId = ResourceLocationArgument.getId(ctx, "boss_id").toString();
        boolean defeated = BoolArgumentType.getBool(ctx, "defeated");
        CommandSourceStack source = ctx.getSource();

        if (!ServerBossIdsLoader.isBoss(bossId)) {
            throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(
                Component.literal(Component.translatable("command.boss_checklist.invalid_boss_id").getString() + bossId)
            ).create();
        }

        ServerSender.sendDefeatedBossDataToAllPlayers(source.getLevel(), bossId, "", defeated, "", "", "#####", "#####");

        ServerStorage.markBoss(bossId, "", null, defeated);

        source.sendSuccess(() ->
            Component.translatable("command.boss_checklist.boss_marked_successfully_" + (defeated ? "true" : "false"), bossId),
            true
        );

        return 1;
    }
}