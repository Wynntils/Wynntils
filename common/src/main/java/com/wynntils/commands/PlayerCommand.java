/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.players.type.GuildRank;
import com.wynntils.utils.SimpleDateFormatter;
import com.wynntils.utils.mc.McUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PlayerCommand extends Command {
    private static final SimpleDateFormatter DATE_FORMATTER = new SimpleDateFormatter();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
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
                    getPlayerGuildJson(context.getArgument("username", String.class));

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
                    getPlayerLastSeenJson(context.getArgument("username", String.class));

            completableFuture.whenComplete((result, throwable) -> McUtils.sendMessageToClient(result));
        });

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.player.lookingUp")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private static CompletableFuture<MutableComponent> getPlayerGuildJson(String username) {
        CompletableFuture<MutableComponent> future = new CompletableFuture<>();

        ApiResponse playerApiResponse = Managers.Net.callApi(UrlId.DATA_WYNNCRAFT_PLAYER, Map.of("username", username));
        playerApiResponse.handleJsonObject(
                playerJson -> {
                    if (playerJson.has("Error")) {
                        future.complete(
                                Component.literal("Unknown player " + username).withStyle(ChatFormatting.RED));
                    } else if (!playerJson.has("username")) { // Handles multi selector
                        // Display all UUID's for known players with this username
                        // with click events to run the command with the UUID instead.
                        // Multi selector doesn't give any other identifiable
                        // information besides rank which doesn't really help
                        MutableComponent response = Component.literal("Multiple players found with the username ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(username).withStyle(ChatFormatting.RED))
                                .append(Component.literal(":").withStyle(ChatFormatting.GRAY));

                        for (String uuid : playerJson.keySet()) {
                            MutableComponent current = Component.literal("\n")
                                    .append(Component.literal(uuid)
                                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.UNDERLINE));

                            current.withStyle(style -> style.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/player guild " + uuid)));

                            response.append(current);
                        }

                        future.complete(response);
                    } else {
                        MutableComponent response = Component.literal(
                                        playerJson.get("username").getAsString())
                                .withStyle(ChatFormatting.DARK_AQUA);

                        if (!playerJson.get("guild").isJsonNull()) {
                            JsonObject guildInfo = playerJson.getAsJsonObject("guild");
                            String name = guildInfo.get("name").getAsString();
                            String prefix = guildInfo.get("prefix").getAsString();

                            GuildRank guildRank =
                                    GuildRank.fromName(guildInfo.get("rank").getAsString());

                            response.append(Component.literal(" is a ")
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal(guildRank.getGuildDescription())
                                            .withStyle(ChatFormatting.AQUA)
                                            .append(Component.literal(" of ")
                                                    .withStyle(ChatFormatting.GRAY)
                                                    .append(Component.literal(name + " [" + prefix + "]")
                                                            .withStyle(ChatFormatting.AQUA)))));

                            ApiResponse guildApiResponse =
                                    Managers.Net.callApi(UrlId.DATA_WYNNCRAFT_GUILD, Map.of("name", name));
                            guildApiResponse.handleJsonObject(
                                    guildJson -> {
                                        if (!guildJson.has("name")) {
                                            future.complete(response);
                                            return;
                                        }

                                        String joined = guildJson
                                                .getAsJsonObject("members")
                                                .getAsJsonObject(
                                                        guildRank.getName().toLowerCase(Locale.ROOT))
                                                .getAsJsonObject(playerJson
                                                        .get("username")
                                                        .getAsString())
                                                .get("joined")
                                                .getAsString();

                                        try {
                                            Date joinedDate = DATE_FORMAT.parse(joined);
                                            long differenceInMillis = System.currentTimeMillis() - joinedDate.getTime();

                                            response.append(Component.literal("\nThey have been in the guild for ")
                                                    .withStyle(ChatFormatting.GRAY)
                                                    .append(Component.literal(DATE_FORMATTER.format(differenceInMillis))
                                                            .withStyle(ChatFormatting.AQUA)));
                                        } catch (ParseException e) {
                                            WynntilsMod.error(
                                                    "Error when trying to parse player joined guild date.", e);
                                        }

                                        future.complete(response);
                                    },
                                    onError -> future.complete(response));
                        } else {
                            response.append(
                                    Component.literal(" is not in a guild").withStyle(ChatFormatting.GRAY));

                            future.complete(response);
                        }
                    }
                },
                onError -> future.complete(Component.literal("Unable to get player guild for " + username)
                        .withStyle(ChatFormatting.RED)));

        return future;
    }

    private static CompletableFuture<MutableComponent> getPlayerLastSeenJson(String username) {
        CompletableFuture<MutableComponent> future = new CompletableFuture<>();

        ApiResponse playerApiResponse = Managers.Net.callApi(UrlId.DATA_WYNNCRAFT_PLAYER, Map.of("username", username));
        playerApiResponse.handleJsonObject(
                playerJson -> {
                    if (playerJson.has("Error")) {
                        future.complete(
                                Component.literal("Unknown player " + username).withStyle(ChatFormatting.RED));
                    } else if (!playerJson.has("username")) { // Handles multi selector
                        // Display all UUID's for known players with this username
                        // with click events to run the command with the UUID instead.
                        // Multi selector doesn't give any other identifiable
                        // information besides rank which doesn't really help
                        MutableComponent response = Component.literal("Multiple players found with the username ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(username).withStyle(ChatFormatting.RED))
                                .append(Component.literal(":").withStyle(ChatFormatting.GRAY));

                        for (String uuid : playerJson.keySet()) {
                            MutableComponent current = Component.literal("\n")
                                    .append(Component.literal(uuid)
                                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.UNDERLINE));

                            current.withStyle(style -> style.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/player lastseen " + uuid)));

                            response.append(current);
                        }

                        future.complete(response);
                    } else {
                        MutableComponent response;

                        if (playerJson.get("online").getAsBoolean()) {
                            response = Component.literal(
                                            playerJson.get("username").getAsString())
                                    .withStyle(ChatFormatting.AQUA)
                                    .append(Component.literal(" is online on ")
                                            .withStyle(ChatFormatting.GRAY)
                                            .append(Component.literal(playerJson
                                                            .get("server")
                                                            .getAsString())
                                                    .withStyle(ChatFormatting.GOLD)));
                        } else {
                            try {
                                Date joinedDate = DATE_FORMAT.parse(
                                        playerJson.get("lastJoin").getAsString());
                                long differenceInMillis = System.currentTimeMillis() - joinedDate.getTime();

                                response = Component.literal(
                                                playerJson.get("username").getAsString())
                                        .withStyle(ChatFormatting.AQUA)
                                        .append(Component.literal(" was last seen ")
                                                .withStyle(ChatFormatting.GRAY))
                                        .append(Component.literal(DATE_FORMATTER.format(differenceInMillis))
                                                .withStyle(ChatFormatting.GOLD)
                                                .append(Component.literal("ago").withStyle(ChatFormatting.GRAY)));
                            } catch (ParseException e) {
                                WynntilsMod.error("Error when trying to parse player last join.", e);
                                response = Component.literal("Failed to get player last seen")
                                        .withStyle(ChatFormatting.RED);
                            }
                        }

                        future.complete(response);
                    }
                },
                onError -> future.complete(Component.literal("Unable to get player last seen for " + username)
                        .withStyle(ChatFormatting.RED)));

        return future;
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }
}
