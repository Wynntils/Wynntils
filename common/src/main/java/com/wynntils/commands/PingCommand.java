/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class PingCommand extends Command {
    private static final SuggestionProvider<CommandSourceStack> PARTY_MEMBER_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Models.Party.getPartyMembers().stream()
                            .filter(member -> !member.equals(McUtils.playerName()))
                            .toList(),
                    builder);

    private static final SuggestionProvider<CommandSourceStack> IGNORED_PLAYER_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(Services.Hades.getIgnoredPingUsers(), builder);

    @Override
    public String getCommandName() {
        return "ping";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext context) {
        return base.then(Commands.literal("ignore")
                        .then(Commands.literal("add")
                                .then(Commands.argument("username", StringArgumentType.word())
                                        .suggests(PARTY_MEMBER_SUGGESTION_PROVIDER)
                                        .executes(this::addIgnoredPingUser)))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("username", StringArgumentType.word())
                                        .suggests(IGNORED_PLAYER_SUGGESTION_PROVIDER)
                                        .executes(this::removeIgnoredPingUser)))
                        .then(Commands.literal("clear").executes(this::clearIgnoredPingUsers))
                        .then(Commands.literal("list").executes(this::listIgnoredPingUsers)))
                .executes(this::syntaxError);
    }

    private int addIgnoredPingUser(CommandContext<CommandSourceStack> context) {
        String username = context.getArgument("username", String.class);

        Services.Hades.addIgnoredPingUser(username);

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.ping.add1")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(username).withStyle(ChatFormatting.AQUA))
                                .append(Component.translatable("command.wynntils.ping.add2")
                                        .withStyle(ChatFormatting.GRAY)),
                        false);

        return 1;
    }

    private int removeIgnoredPingUser(CommandContext<CommandSourceStack> context) {
        String username = context.getArgument("username", String.class);

        if (!Services.Hades.getIgnoredPingUsers().contains(username)) {
            context.getSource()
                    .sendFailure(Component.translatable("command.wynntils.ping.notCurrentlyIgnored")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        Services.Hades.removeIgnoredPingUser(username);

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.ping.remove1")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(username).withStyle(ChatFormatting.AQUA))
                                .append(Component.translatable("command.wynntils.ping.remove2")
                                        .withStyle(ChatFormatting.GRAY)),
                        false);

        return 1;
    }

    private int clearIgnoredPingUsers(CommandContext<CommandSourceStack> context) {
        Services.Hades.clearIgnoredPingUsers();

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.ping.removedAll")
                                .withStyle(ChatFormatting.GRAY),
                        false);

        return 1;
    }

    private int listIgnoredPingUsers(CommandContext<CommandSourceStack> context) {
        List<String> ignoredUsers = new ArrayList<>(Services.Hades.getIgnoredPingUsers());

        if (ignoredUsers.isEmpty()) {
            context.getSource()
                    .sendSuccess(
                            () -> Component.translatable("command.wynntils.ping.noIgnored")
                                    .withStyle(ChatFormatting.GRAY),
                            false);
            return 1;
        }

        Component response =
                Component.translatable("command.wynntils.ping.ignoredUsers").withStyle(ChatFormatting.GRAY);

        for (int i = 0; i < ignoredUsers.size(); i++) {
            if (i > 0) {
                response = response.copy().append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }

            response = response.copy()
                    .append(Component.literal(ignoredUsers.get(i)).withStyle(ChatFormatting.AQUA));
        }

        Component finalResponse = response;
        context.getSource().sendSuccess(() -> finalResponse, false);

        return 1;
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }
}
