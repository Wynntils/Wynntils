/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.models.worlds.profile.ServerProfile;
import com.wynntils.utils.StringUtils;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class ServersCommand extends Command {
    private static final int UPDATE_TIME_OUT_MS = 3000;

    @Override
    public String getCommandName() {
        return "servers";
    }

    @Override
    protected List<String> getAliases() {
        return List.of("s", "srv");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base) {
        LiteralCommandNode<CommandSourceStack> listBuilder = Commands.literal("list")
                .then(Commands.literal("up").executes(this::serverUptimeList))
                .executes(this::serverList)
                .build();

        LiteralCommandNode<CommandSourceStack> infoBuilder = Commands.literal("info")
                .then(Commands.argument("server", StringArgumentType.word()).executes(this::serverInfo))
                .build();

        LiteralCommandNode<CommandSourceStack> soulpointsBuilder = Commands.literal("soulpoints")
                .executes(this::serverSoulpointsList)
                .build();

        return base.then(listBuilder)
                .then(infoBuilder)
                .then(Commands.literal("l").executes(this::serverList))
                .then(Commands.literal("ul").executes(this::serverUptimeList))
                .then(Commands.literal("up").executes(this::serverUptimeList))
                .then(Commands.literal("soul").executes(this::serverSoulpointsList))
                .then(Commands.literal("s").executes(this::serverSoulpointsList))
                .then(Commands.literal("i").redirect(infoBuilder))
                .executes(this::syntaxError);
    }

    private int serverInfo(CommandContext<CommandSourceStack> context) {
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
        MutableComponent message = Component.empty()
                .append(getServerComponent(server).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(":" + "\n")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("Uptime: ")
                                .withStyle(ChatFormatting.DARK_AQUA)
                                .append(Component.literal(serverProfile.getUptime() + "\n")
                                        .withStyle(ChatFormatting.AQUA)))
                        .append(Component.literal("Online players on ")
                                .withStyle(ChatFormatting.DARK_AQUA)
                                .append(Component.literal(server).withStyle(ChatFormatting.AQUA))
                                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_AQUA))
                                .append(Component.literal(players.size() + "\n").withStyle(ChatFormatting.AQUA))));

        if (players.isEmpty()) {
            message.append(Component.literal("No players!").withStyle(ChatFormatting.AQUA));
        } else {
            message.append(Component.literal(String.join(", ", players)).withStyle(ChatFormatting.AQUA));
        }

        context.getSource().sendSuccess(() -> message, false);

        return 1;
    }

    private int serverList(CommandContext<CommandSourceStack> context) {
        MutableComponent message = Component.literal("Server list:").withStyle(ChatFormatting.DARK_AQUA);

        for (String serverType : Models.ServerList.getWynnServerTypes()) {
            List<String> currentTypeServers = Models.ServerList.getServersSortedOnNameOfType(serverType);

            if (currentTypeServers.isEmpty()) continue;

            message.append("\n");

            message.append(Component.literal(
                            StringUtils.capitalizeFirst(serverType) + " (" + currentTypeServers.size() + "):\n")
                    .withStyle(ChatFormatting.GOLD));

            String lastServer = currentTypeServers.get(currentTypeServers.size() - 1);
            for (String server : currentTypeServers) {
                message.append(getServerComponent(server).withStyle(ChatFormatting.AQUA));

                if (!server.equals(lastServer)) {
                    message.append(Component.literal(", ").withStyle(ChatFormatting.DARK_AQUA));
                }
            }
        }

        context.getSource().sendSuccess(() -> message, false);

        return 1;
    }

    private int serverUptimeList(CommandContext<CommandSourceStack> context) {
        List<String> sortedServers = Models.ServerList.getServersSortedOnUptime();

        MutableComponent message = Component.literal("Server list:").withStyle(ChatFormatting.GOLD);
        for (String server : sortedServers) {
            message.append("\n");
            message.append(getServerComponent(server)
                    .withStyle(ChatFormatting.DARK_AQUA)
                    .append(Component.literal(
                                    ": " + Models.ServerList.getServer(server).getUptime())
                            .withStyle(ChatFormatting.AQUA)));
        }

        context.getSource().sendSuccess(() -> message, false);

        return 1;
    }

    private int serverSoulpointsList(CommandContext<CommandSourceStack> context) {
        List<ServerProfile> soulPointServers = Models.ServerList.getServersSortedOnUptime().stream()
                .map(Models.ServerList::getServer)
                .filter(Objects::nonNull)
                .filter(server -> server.getUptimeInMinutes() % 20 >= 10)
                .toList();

        MutableComponent message = Component.literal("Soul point server list:").withStyle(ChatFormatting.GOLD);
        for (ServerProfile server : soulPointServers) {
            int minutesUntilSoulPoint = 20 - (server.getUptimeInMinutes() % 20);

            // 1-2 minutes before - Green
            // 3-4 minutes before - Yellow
            // 5+ minutes before - Red
            ChatFormatting timeColor = minutesUntilSoulPoint <= 2
                    ? ChatFormatting.GREEN
                    : (minutesUntilSoulPoint <= 4 ? ChatFormatting.YELLOW : ChatFormatting.RED);

            message.append("\n");
            message.append(getServerComponent(server.getServerName())
                    .withStyle(ChatFormatting.DARK_AQUA)
                    .append(Component.literal(": In " + minutesUntilSoulPoint + "m")
                            .withStyle(timeColor)));
        }

        context.getSource().sendSuccess(() -> message, false);

        return 1;
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }

    private MutableComponent getServerComponent(String server) {
        return Component.literal(server)
                .withStyle(style -> style.withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.literal("Click to switch to ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(server).withStyle(ChatFormatting.WHITE))
                                .append(Component.literal("\n(Requires ")
                                        .withStyle(ChatFormatting.DARK_PURPLE)
                                        .append(Component.literal("HERO").withStyle(ChatFormatting.LIGHT_PURPLE))
                                        .append(Component.literal(" rank)").withStyle(ChatFormatting.DARK_PURPLE))))))
                .withStyle(style ->
                        style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/switch " + server)));
    }
}
