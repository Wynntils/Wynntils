/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PlayerCommand extends Command {
    private static final SuggestionProvider<CommandSourceStack> PLAYER_NAME_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(Models.Player.getAllPlayerNames(), builder);

    @Override
    public String getCommandName() {
        return "player";
    }

    @Override
    public List<String> getAliases() {
        return List.of("pl");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base) {
        return base.then(Commands.literal("guild")
                        .then(Commands.argument("username", StringArgumentType.word())
                                .suggests(PLAYER_NAME_SUGGESTION_PROVIDER)
                                .executes(this::lookupPlayerGuild)))
                .then(Commands.literal("g")
                        .then(Commands.argument("username", StringArgumentType.word())
                                .suggests(PLAYER_NAME_SUGGESTION_PROVIDER)
                                .executes(this::lookupPlayerGuild)))
                .then(Commands.literal("lastseen")
                        .then(Commands.argument("username", StringArgumentType.word())
                                .suggests(PLAYER_NAME_SUGGESTION_PROVIDER)
                                .executes(this::lookupPlayerLastSeen)))
                .then(Commands.literal("ls")
                        .then(Commands.argument("username", StringArgumentType.word())
                                .suggests(PLAYER_NAME_SUGGESTION_PROVIDER)
                                .executes(this::lookupPlayerLastSeen)))
                .executes(this::syntaxError);
    }

    private int lookupPlayerGuild(CommandContext<CommandSourceStack> context) {
        CompletableFuture.runAsync(() -> {
            CompletableFuture<MutableComponent> completableFuture =
                    Models.Player.getPlayerGuild(context.getArgument("username", String.class));

            completableFuture.whenComplete((result, throwable) -> McUtils.sendMessageToClient(result));
        });

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.player.lookingUp")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private int lookupPlayerLastSeen(CommandContext<CommandSourceStack> context) {
        CompletableFuture.runAsync(() -> {
            CompletableFuture<MutableComponent> completableFuture =
                    Models.Player.getPlayerLastSeen(context.getArgument("username", String.class));

            completableFuture.whenComplete((result, throwable) -> McUtils.sendMessageToClient(result));
        });

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.player.lookingUp")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }
}
