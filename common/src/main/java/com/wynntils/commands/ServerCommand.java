/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.commands.WynntilsCommandBase;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.utils.StringUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class ServerCommand extends WynntilsCommandBase {
    private final List<String> serverTypes =
            Lists.newArrayList("WC", "lobby", "GM", "DEV", "WAR", "HB");

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> listNode =
                Commands.literal("list").executes(this::serverList).build();

        LiteralCommandNode<CommandSourceStack> infoNode =
                Commands.literal("info")
                        .then(
                                Commands.argument("server", StringArgumentType.word())
                                        .executes(this::serverInfo))
                        .executes(this::serverInfoHelp)
                        .build();
        LiteralCommandNode<CommandSourceStack> node =
                dispatcher.register(
                        Commands.literal("server")
                                .then(listNode)
                                .then(Commands.literal("ls").redirect(listNode))
                                .then(Commands.literal("l").redirect(listNode))
                                .then(infoNode)
                                .then(Commands.literal("i").redirect(infoNode))
                                .executes(this::serverHelp));

        dispatcher.register(Commands.literal("s").redirect(node));
    }

    private int serverInfoHelp(CommandContext<CommandSourceStack> context) {
        context.getSource()
                .sendFailure(
                        new TextComponent("Usage: /s i,info <server> | Example: \"/s i WC1\"")
                                .withStyle(ChatFormatting.RED));
        return 1;
    }

    private int serverHelp(CommandContext<CommandSourceStack> context) {
        MutableComponent text =
                new TextComponent(
                                """
                                /s <command> [options]

                                commands:
                                l,ls,list | list available servers
                                i,info | get info about a server

                                more detailed help:
                                /s <command> help""")
                        .withStyle(ChatFormatting.RED);

        context.getSource().sendSuccess(text, false);

        return 1;
    }

    private int serverInfo(CommandContext<CommandSourceStack> context) {
        Map<String, List<String>> onlinePlayers;
        try {
            onlinePlayers = WebManager.getOnlinePlayers();
        } catch (IOException e) {
            context.getSource()
                    .sendFailure(
                            new TextComponent("Failed to get server data from API.")
                                    .withStyle(ChatFormatting.RED));
            return 1;
        }

        String server = context.getArgument("server", String.class);

        if (server.startsWith("wc")) server = server.toUpperCase(Locale.ROOT);

        try {
            int serverNum = Integer.parseInt(server);
            server = "WC" + serverNum;
        } catch (Exception ignored) {
        }

        if (!onlinePlayers.containsKey(server)) {
            context.getSource()
                    .sendFailure(
                            new TextComponent(server + " not found.")
                                    .withStyle(ChatFormatting.RED));
            return 1;
        }

        List<String> players = onlinePlayers.get(server);
        MutableComponent message =
                new TextComponent("Online players on " + server + ": " + players.size() + "\n")
                        .withStyle(ChatFormatting.DARK_AQUA);

        if (players.size() == 0) {
            message.append(new TextComponent("No players!").withStyle(ChatFormatting.AQUA));
        } else {
            message.append(
                    new TextComponent(String.join(", ", players)).withStyle(ChatFormatting.AQUA));
        }

        context.getSource().sendSuccess(message, false);

        return 1;
    }

    private int serverList(CommandContext<CommandSourceStack> context) {
        HashMap<String, List<String>> onlinePlayers;
        try {
            onlinePlayers = WebManager.getOnlinePlayers();
        } catch (IOException e) {
            context.getSource()
                    .sendFailure(
                            new TextComponent("Failed to get server data from API.")
                                    .withStyle(ChatFormatting.RED));
            return 1;
        }

        MutableComponent message =
                new TextComponent("Server list:").withStyle(ChatFormatting.DARK_AQUA);

        for (String serverType : serverTypes) {
            List<String> currentTypeServers =
                    onlinePlayers.keySet().stream()
                            .filter(server -> server.startsWith(serverType))
                            .sorted(
                                    (o1, o2) -> {
                                        int number1 =
                                                Integer.parseInt(o1.substring(serverType.length()));
                                        int number2 =
                                                Integer.parseInt(o2.substring(serverType.length()));

                                        return number1 - number2;
                                    })
                            .toList();

            if (currentTypeServers.isEmpty()) continue;

            message.append("\n");

            message.append(
                    new TextComponent(
                                    StringUtils.capitalizeFirst(serverType)
                                            + " ("
                                            + currentTypeServers.size()
                                            + "):\n")
                            .withStyle(ChatFormatting.GOLD));

            message.append(
                    new TextComponent(String.join(", ", currentTypeServers))
                            .withStyle(ChatFormatting.AQUA));
        }

        context.getSource().sendSuccess(message, false);

        return 1;
    }
}
