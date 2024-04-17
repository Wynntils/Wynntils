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
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.players.type.GuildRank;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                        .executes(this::getGuildJson))
                .executes(this::syntaxError);
    }

    private int getGuildJson(CommandContext<CommandSourceStack> context) {
        CompletableFuture.runAsync(() -> {
            CompletableFuture<MutableComponent> completableFuture =
                    getGuildJson(context.getArgument("guildName", String.class));

            completableFuture.whenComplete((result, throwable) -> McUtils.sendMessageToClient(result));
        });

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.onlineMembers.lookingUp")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private static CompletableFuture<MutableComponent> getGuildJson(String inputName) {
        CompletableFuture<MutableComponent> future = new CompletableFuture<>();

        String guildName = Models.Guild.getGuildNameFromString(inputName);

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.DATA_WYNNCRAFT_GUILD, Map.of("name", guildName));
        apiResponse.handleJsonObject(
                json -> {
                    if (!json.has("name")) {
                        future.complete(Component.literal("Unable to check online members for " + guildName)
                                .withStyle(ChatFormatting.RED));
                        return;
                    }

                    MutableComponent response = Component.literal(
                                    json.get("name").getAsString() + " ["
                                            + json.get("prefix").getAsString() + "]")
                            .withStyle(ChatFormatting.DARK_AQUA);

                    response.append(Component.literal(" has ").withStyle(ChatFormatting.GRAY));

                    JsonObject guildMembers = json.getAsJsonObject("members");

                    int totalCount = guildMembers.get("total").getAsInt();
                    int onlineCount = json.get("online").getAsInt();

                    response.append(Component.literal(onlineCount + "/" + totalCount)
                                    .withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(" members currently online:")
                                    .withStyle(ChatFormatting.GRAY));

                    for (String rank : guildMembers.keySet()) {
                        if (rank.equals("total")) continue;

                        GuildRank guildRank = GuildRank.fromName(rank);

                        JsonObject roleMembers = guildMembers.getAsJsonObject(rank);

                        List<String> onlineMembers = new ArrayList<>();

                        for (String username : roleMembers.keySet()) {
                            JsonObject memberInfo = roleMembers.getAsJsonObject(username);

                            if (memberInfo.get("online").getAsBoolean()) {
                                onlineMembers.add(username);
                            }
                        }

                        if (onlineMembers.isEmpty()) continue;

                        if (guildRank != null) {
                            response.append(Component.literal("\n" + guildRank.getGuildDescription() + ":\n")
                                    .withStyle(ChatFormatting.GOLD));
                        }

                        for (String guildMember : onlineMembers) {
                            response.append(Component.literal(guildMember).withStyle(ChatFormatting.AQUA));

                            if (onlineMembers.indexOf(guildMember) != onlineMembers.size() - 1) {
                                response.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }

                    future.complete(response);
                },
                onError -> future.complete(Component.literal("Unable to check online members for " + guildName)
                        .withStyle(ChatFormatting.RED)));

        return future;
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }
}
