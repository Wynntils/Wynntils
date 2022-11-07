/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.webapi.ServerListModel;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.ServerProfile;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.objects.account.PlayerAccount;
import com.wynntils.wynn.utils.WynnUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Style;
public class ServerCommand extends CommandBase {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(getBaseCommandBuilder());

        dispatcher.register(Commands.literal("s").redirect(node));
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        LiteralCommandNode<CommandSourceStack> listNode =
                Commands.literal("list").executes(this::serverList).build();

        LiteralCommandNode<CommandSourceStack> infoNode = Commands.literal("info")
                .then(Commands.argument("server", StringArgumentType.word()).executes(this::serverInfo))
                .executes(this::serverInfoHelp)
                .build();

        LiteralCommandNode<CommandSourceStack> soulPointNode = Commands.literal("sp")
                .then(Commands.argument("offset", IntegerArgumentType.integer(-20, 20))
                        .executes(this::soulPointCommand))
                .executes(this::soulPointCommand)
                .build();

        return Commands.literal("server")
                .then(listNode)
                .then(Commands.literal("ls").redirect(listNode))
                .then(Commands.literal("l").redirect(listNode))
                .then(infoNode)
                .then(Commands.literal("i").redirect(infoNode))
                .then(soulPointNode)
                .then(Commands.literal("soulpoint").redirect(soulPointNode))
                .executes(this::serverHelp);
    }

    private int serverInfoHelp(CommandContext<CommandSourceStack> context) {
        context.getSource()
                .sendFailure(new TextComponent("Usage: /s i,info <server> | Example: \"/s i WC1\"")
                        .withStyle(ChatFormatting.RED));
        return 1;
    }

    private int serverHelp(CommandContext<CommandSourceStack> context) {
        MutableComponent text = new TextComponent(
                        """
                        /s <command> [options]

                        commands:
                        l,ls,list | list available servers
                        i,info | get info about a server
                        sp, soulpoint | get the next 5 soul point worlds

                        more detailed help:
                        /s <command> help""")
                .withStyle(ChatFormatting.RED);

        context.getSource().sendSuccess(text, false);

        return 1;
    }

    private int soulPointCommand(CommandContext<CommandSourceStack> context) {
        Map<String, Integer> nextServers = new HashMap<>();

        int soulPointTime = 20;

        int offset = 0;
        try {
            offset = context.getArgument("offset", Integer.class);
        } catch (IllegalArgumentException ignored) {
        }

        for (Map.Entry<String, ServerProfile> entry :
                ServerListModel.getAvailableServers().entrySet()) {
            int time = soulPointTime - ((entry.getValue().getUptimeMinutes() + offset) % soulPointTime);
            if (time > soulPointTime) {
                time = time - soulPointTime;
            }
            nextServers.put(entry.getKey(), time);
        }
        LinkedHashMap<String, Integer> sortedServers = nextServers.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        Component soulPointInfo = new TextComponent("Approximate soul point times: ");
        soulPointInfo.getStyle().applyFormats(ChatFormatting.BOLD, ChatFormatting.AQUA);
        context.getSource().sendSuccess(soulPointInfo, false);

        sortedServers.entrySet().stream().limit(10).map(Map.Entry::getKey).forEach(server -> {
            int uptimeMinutes = sortedServers.get(server);
            ChatFormatting minuteColor;
            if (uptimeMinutes <= 2) {
                minuteColor = ChatFormatting.GREEN;
            } else if (uptimeMinutes <= 5) {
                minuteColor = ChatFormatting.YELLOW;
            } else {
                minuteColor = ChatFormatting.RED;
            }
            MutableComponent world = new TextComponent(ChatFormatting.BOLD + "-" + ChatFormatting.GOLD);
            TextComponent serverLine = new TextComponent(ChatFormatting.BLUE + server + "- ");
            WebManager.updatePlayerStats();
            PlayerAccount playerAccount = WebManager.getPlayerAccount();
            if (playerAccount.getTag().isHeroPlus()) {
                ClickEvent switchCommand = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/switch " + server);
                HoverEvent switchCommandHover = new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT, new TextComponent("Switch to " + ChatFormatting.BLUE + server));
                Style style =
                        serverLine.getStyle().withHoverEvent(switchCommandHover).withClickEvent(switchCommand);
                serverLine.withStyle(style);
                world.append(serverLine);
            } else {
                serverLine
                        .getStyle()
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new TextComponent(
                                        ChatFormatting.RED + "HERO or higher rank is required to use /switch")));
                world.append(serverLine);
            }
            world.append(ChatFormatting.AQUA + " in " + minuteColor + uptimeMinutes
                    + (uptimeMinutes == 1 || uptimeMinutes == 0 ? " minute" : " minutes"));
            context.getSource().sendSuccess(world, false);
        });

        return 1;
    }

    private int serverInfo(CommandContext<CommandSourceStack> context) {
        Map<String, List<String>> onlinePlayers;
        try {
            onlinePlayers = WebManager.getOnlinePlayers();
        } catch (IOException e) {
            context.getSource()
                    .sendFailure(
                            new TextComponent("Failed to get server data from API.").withStyle(ChatFormatting.RED));
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
            context.getSource().sendFailure(new TextComponent(server + " not found.").withStyle(ChatFormatting.RED));
            return 1;
        }

        List<String> players = onlinePlayers.get(server);
        MutableComponent message = new TextComponent("Online players on " + server + ": " + players.size() + "\n")
                .withStyle(ChatFormatting.DARK_AQUA);

        if (players.isEmpty()) {
            message.append(new TextComponent("No players!").withStyle(ChatFormatting.AQUA));
        } else {
            message.append(new TextComponent(String.join(", ", players)).withStyle(ChatFormatting.AQUA));
        }

        context.getSource().sendSuccess(message, false);

        return 1;
    }

    private int serverList(CommandContext<CommandSourceStack> context) {
        Map<String, List<String>> onlinePlayers;
        try {
            onlinePlayers = WebManager.getOnlinePlayers();
        } catch (IOException e) {
            context.getSource()
                    .sendFailure(
                            new TextComponent("Failed to get server data from API.").withStyle(ChatFormatting.RED));
            return 1;
        }

        MutableComponent message = new TextComponent("Server list:").withStyle(ChatFormatting.DARK_AQUA);

        for (String serverType : WynnUtils.getWynnServerTypes()) {
            List<String> currentTypeServers = onlinePlayers.keySet().stream()
                    .filter(server -> server.startsWith(serverType))
                    .sorted((o1, o2) -> {
                        int number1 = Integer.parseInt(o1.substring(serverType.length()));
                        int number2 = Integer.parseInt(o2.substring(serverType.length()));

                        return number1 - number2;
                    })
                    .toList();

            if (currentTypeServers.isEmpty()) continue;

            message.append("\n");

            message.append(new TextComponent(
                            StringUtils.capitalizeFirst(serverType) + " (" + currentTypeServers.size() + "):\n")
                    .withStyle(ChatFormatting.GOLD));

            message.append(new TextComponent(String.join(", ", currentTypeServers)).withStyle(ChatFormatting.AQUA));
        }

        context.getSource().sendSuccess(message, false);

        return 1;
    }
}
