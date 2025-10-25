package com.yori3o.boss_checklist.commands;

import com.yori3o.boss_checklist.BossChecklist;
import com.yori3o.boss_checklist.network.BossDefeatServerSend;
import com.yori3o.boss_checklist.server.BossDefeatedData;
import com.yori3o.boss_checklist.server.ServerBossIdsLoader;
import com.yori3o.boss_checklist.utils.LoggerUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import java.util.concurrent.CompletableFuture;


public class BossChecklistCommands {

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("boss_checklist")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("set_defeated")
                    .then(Commands.argument("boss_id", ResourceLocationArgument.id())
                        .suggests(BossChecklistCommands::suggestBossIds)
                        .then(Commands.argument("defeated", BoolArgumentType.bool())
                            .executes(BossChecklistCommands::executeSetDefeated)
                        )
                    )
                )
        );
    }

    private static CompletableFuture<Suggestions> suggestBossIds(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (String id : ServerBossIdsLoader.server_bosses_ids_list_only_loaded_mods) {
            builder.suggest(id);
        }
        return builder.buildFuture();
    }

    private static int executeSetDefeated(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String bossId = ResourceLocationArgument.getId(ctx, "boss_id").toString();
        boolean defeated = BoolArgumentType.getBool(ctx, "defeated");
        CommandSourceStack source = ctx.getSource();

        if (!ServerBossIdsLoader.server_bosses_ids_list_only_loaded_mods.contains(bossId)) {
            throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(
                Component.literal(Component.translatable("command.boss_checklist.boss_marked_successfully").getString() + bossId)
            ).create();
        }

        BossDefeatServerSend.onBossOrMinibossKilled(source.getLevel(), bossId, "", defeated);

        String value = bossId + "##" + (defeated ? "true" : "false");
        BossDefeatedData data = BossDefeatedData.get(source.getLevel().getServer().overworld());
        if (defeated) {
            data.addBoss(value);
            BossChecklist.defeated_bosses_on_server.add(value);
        } else {
            data.removeBoss(value);
            LoggerUtil.LOGGER.info("boss undefeating its value - " + value);
            BossChecklist.removeBossFromDefeated_bosses_on_server(value);
        }

        String[] text = Component.translatable("command.boss_checklist.boss_marked_successfully").getString().split("%");

        source.sendSuccess(() ->
            Component.literal(text[0] + bossId + text[1] + (defeated ? "defeated" : "undefeated") + text[2]),
            true
        );

        return 1;
    }
}