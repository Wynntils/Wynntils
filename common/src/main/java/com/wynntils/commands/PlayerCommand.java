/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.models.players.type.wynnplayer.PlayerGuildInfo;
import com.wynntils.models.players.type.wynnplayer.WynnPlayerInfo;
import com.wynntils.utils.DateFormatter;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class PlayerCommand extends Command {
    private static final DateFormatter DATE_FORMATTER = new DateFormatter(true);
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
            LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext context) {
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
        CompletableFuture<WynnPlayerInfo> completableFuture =
                Models.Player.getPlayer(context.getArgument("username", String.class));

        completableFuture.whenComplete((player, throwable) -> {
            if (throwable != null) {
                McUtils.sendMessageToClient(Component.literal(
                                "Unable to view player guild for " + context.getArgument("username", String.class))
                        .withStyle(ChatFormatting.RED));
                WynntilsMod.error("Error trying to parse player guild", throwable);
            } else {
                if (player == null) {
                    McUtils.sendMessageToClient(
                            Component.literal("Unknown player " + context.getArgument("username", String.class))
                                    .withStyle(ChatFormatting.RED));
                    return;
                }

                MutableComponent response = Component.literal(player.username()).withStyle(ChatFormatting.DARK_AQUA);

                if (player.guildInfo().isPresent()) {
                    PlayerGuildInfo playerGuildInfo = player.guildInfo().get();

                    response.append(Component.literal(" is a ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(
                                            playerGuildInfo.guildRank().getGuildDescription())
                                    .withStyle(ChatFormatting.AQUA)
                                    .append(Component.literal(" of ")
                                            .withStyle(ChatFormatting.GRAY)
                                            .append(Component.literal(playerGuildInfo.guildName() + " ["
                                                            + playerGuildInfo.guildPrefix() + "]")
                                                    .withStyle(ChatFormatting.AQUA)))));

                    // Should only be null if the player lookup succeeded but the guild lookup did not
                    if (playerGuildInfo.guildJoinTimestamp().isPresent()) {
                        long differenceInMillis = System.currentTimeMillis()
                                - playerGuildInfo.guildJoinTimestamp().get().toEpochMilli();

                        response.append(Component.literal("\nThey have been in the guild for ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(DATE_FORMATTER.format(differenceInMillis))
                                        .withStyle(ChatFormatting.AQUA)));
                    }
                } else {
                    response.append(Component.literal(" is not in a guild").withStyle(ChatFormatting.GRAY));
                }

                McUtils.sendMessageToClient(response);
            }
        });

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.player.lookingUp")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private int lookupPlayerLastSeen(CommandContext<CommandSourceStack> context) {
        CompletableFuture<WynnPlayerInfo> completableFuture =
                Models.Player.getPlayer(context.getArgument("username", String.class));

        completableFuture.whenComplete((player, throwable) -> {
            if (throwable != null) {
                McUtils.sendMessageToClient(Component.literal(
                                "Unable to view player last seen for " + context.getArgument("username", String.class))
                        .withStyle(ChatFormatting.RED));
                WynntilsMod.error("Error trying to parse player last seen", throwable);
            } else {
                if (player == null) {
                    McUtils.sendMessageToClient(
                            Component.literal("Unknown player " + context.getArgument("username", String.class))
                                    .withStyle(ChatFormatting.RED));
                    return;
                }

                MutableComponent response = Component.literal(player.username()).withStyle(ChatFormatting.AQUA);

                if (player.online()) {
                    response.append(Component.literal(" is online on ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(player.server()).withStyle(ChatFormatting.GOLD)));
                } else if (player.lastJoinTimestamp().isPresent()) {
                    long differenceInMillis = System.currentTimeMillis()
                            - player.lastJoinTimestamp().get().toEpochMilli();

                    response.append(Component.literal(" was last seen ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(DATE_FORMATTER.format(differenceInMillis))
                                    .withStyle(ChatFormatting.GOLD)
                                    .append(Component.literal("ago").withStyle(ChatFormatting.GRAY)));
                } else {
                    response.append(Component.literal(" has made their last login private.")
                                    .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(
                                            "\nIf you think you should be able to see this, add your Wynncraft API Token to ")
                                    .withStyle(ChatFormatting.RED))
                            .append(Component.literal("Wynntils Secrets").withStyle(style -> style.withHoverEvent(
                                            new HoverEvent(
                                                    HoverEvent.Action.SHOW_TEXT,
                                                    Component.literal("Click to open secrets menu.")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils secrets"))
                                    .withColor(ChatFormatting.GOLD)
                                    .withUnderlined(true)));
                }

                McUtils.sendMessageToClient(response);
            }
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
