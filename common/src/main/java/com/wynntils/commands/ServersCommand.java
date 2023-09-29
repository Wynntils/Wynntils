/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.models.worlds.profile.ServerProfile;
import com.wynntils.utils.StringUtils;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ServersCommand extends Command {
    private static final int UPDATE_TIME_OUT_MS = 3000;

    @Override
    public String getCommandName() {
        return "servers";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base) {
        return base.then(Commands.literal("list")
                        .then(Commands.literal("up").executes(this::serverUptimeList))
                        .executes(this::serverList)
                        .build())
                .then(Commands.literal("info")
                        .then(Commands.argument("server", StringArgumentType.word())
                                .executes(this::serverInfo))
                        .build())
                .executes(this::syntaxError);
    }

    private int serverInfo(CommandContext<CommandSourceStack> context) {
        if (!Models.ServerList.forceUpdate(UPDATE_TIME_OUT_MS)) {
            context.getSource()
                    .sendFailure(Component.literal("Network problems; using cached data")
                            .withStyle(ChatFormatting.RED));
        }

        String server = context.getArgument("server", String.class);

        if (server.startsWith("wc")) server = server.toUpperCase(Locale.ROOT);

        try {
            int serverNum = Integer.parseInt(server);
            server = "WC" + serverNum;
        } catch (Exception ignored) {
        }

        ServerProfile serverProfile = Models.ServerList.getServer(server);
        if (serverProfile == null) {
            context.getSource()
                    .sendFailure(Component.literal(server + " not found.").withStyle(ChatFormatting.RED));
            return 1;
        }

        Set<String> players = serverProfile.getPlayers();
        MutableComponent message = Component.literal(server + ":" + "\n")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Uptime: " + serverProfile.getUptime() + "\n")
                        .withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal("Online players on " + server + ": " + players.size() + "\n")
                        .withStyle(ChatFormatting.DARK_AQUA));

        if (players.isEmpty()) {
            message.append(Component.literal("No players!").withStyle(ChatFormatting.AQUA));
        } else {
            message.append(Component.literal(String.join(", ", players)).withStyle(ChatFormatting.AQUA));
        }

        context.getSource().sendSuccess(message, false);

        return 1;
    }

    private int serverList(CommandContext<CommandSourceStack> context) {
        if (!Models.ServerList.forceUpdate(3000)) {
            context.getSource()
                    .sendFailure(Component.literal("Network problems; using cached data")
                            .withStyle(ChatFormatting.RED));
        }

        MutableComponent message = Component.literal("Server list:").withStyle(ChatFormatting.DARK_AQUA);

        for (String serverType : Models.ServerList.getWynnServerTypes()) {
            List<String> currentTypeServers = Models.ServerList.getServersSortedOnNameOfType(serverType);

            if (currentTypeServers.isEmpty()) continue;

            message.append("\n");

            message.append(Component.literal(
                            StringUtils.capitalizeFirst(serverType) + " (" + currentTypeServers.size() + "):\n")
                    .withStyle(ChatFormatting.GOLD));

            message.append(
                    Component.literal(String.join(", ", currentTypeServers)).withStyle(ChatFormatting.AQUA));
        }

        context.getSource().sendSuccess(message, false);

        return 1;
    }

    private int serverUptimeList(CommandContext<CommandSourceStack> context) {
        if (!Models.ServerList.forceUpdate(3000)) {
            context.getSource()
                    .sendFailure(Component.literal("Network problems; using cached data")
                            .withStyle(ChatFormatting.RED));
        }

        List<String> sortedServers = Models.ServerList.getServersSortedOnUptime();

        MutableComponent message = Component.literal("Server list:").withStyle(ChatFormatting.DARK_AQUA);
        for (String server : sortedServers) {
            message.append("\n");
            message.append(Component.literal(
                            server + ": " + Models.ServerList.getServer(server).getUptime())
                    .withStyle(ChatFormatting.AQUA));
        }

        context.getSource().sendSuccess(message, false);

        return 1;
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }
}
