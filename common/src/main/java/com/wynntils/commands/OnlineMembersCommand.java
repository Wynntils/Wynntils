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

public class OnlineMembersCommand extends Command {
    private static final SuggestionProvider<CommandSourceStack> GUILD_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(Models.Guild.getAllGuilds(), builder);

    @Override
    public String getCommandName() {
        return "onlinemembers";
    }

    @Override
    public List<String> getAliases() {
        return List.of("om");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base) {
        return base.then(Commands.argument("guildName", StringArgumentType.greedyString())
                        .suggests(GUILD_SUGGESTION_PROVIDER)
                        .executes(this::lookupGuild))
                .executes(this::syntaxError);
    }

    private int lookupGuild(CommandContext<CommandSourceStack> context) {
        CompletableFuture.runAsync(() -> {
            CompletableFuture<MutableComponent> completableFuture =
                    Models.Guild.getGuildOnlineMembers(context.getArgument("guildName", String.class));

            completableFuture.whenComplete((result, throwable) -> McUtils.sendMessageToClient(result));
        });

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.onlineMembers.lookingUp")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }
}
