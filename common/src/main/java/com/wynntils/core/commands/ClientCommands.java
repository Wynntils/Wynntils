/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wynntils.commands.TestCommand;
import com.wynntils.mc.utils.McUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;

// Credits to Earthcomputer.
// Parts of this code originates from https://github.com/Earthcomputer/clientcommands
public class ClientCommands {

    private static final Set<String> clientSideCommands = new HashSet<>();

    public static void clearClientSideCommands() {
        clientSideCommands.clear();
    }

    public static void addClientSideCommand(String name) {
        clientSideCommands.add(name);
    }

    public static boolean isClientSideCommand(String name) {
        return clientSideCommands.contains(name);
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        ClientCommands.clearClientSideCommands();

        ClientCommands.register(dispatcher, new TestCommand());
    }

    private static void register(
            CommandDispatcher<CommandSourceStack> dispatcher, Command command) {
        command.registerName();
        command.register(dispatcher);
    }

    public static void executeCommand(StringReader reader, String command) {
        LocalPlayer player = McUtils.player();

        if (player == null) return;

        try {
            player.connection.getCommands().execute(reader, new FakeCommandSource(player));
        } catch (CommandRuntimeException e) {
            sendError(new TextComponent(e.getMessage()));
        } catch (CommandSyntaxException e) {
            sendError(new TextComponent(e.getRawMessage().getString()));
            if (e.getInput() != null && e.getCursor() >= 0) {
                int cursor = Math.min(e.getCursor(), e.getInput().length());
                MutableComponent text =
                        new TextComponent("")
                                .withStyle(
                                        Style.EMPTY
                                                .withColor(ChatFormatting.GRAY)
                                                .withClickEvent(
                                                        new ClickEvent(
                                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                                command)));
                if (cursor > 10) text.append("...");

                text.append(e.getInput().substring(Math.max(0, cursor - 10), cursor));
                if (cursor < e.getInput().length()) {
                    text.append(
                            new TextComponent(e.getInput().substring(cursor))
                                    .withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE));
                }

                text.append(
                        new TranslatableComponent("command.context.here")
                                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                ClientCommands.sendError(text);
            }
        } catch (Exception e) {
            TextComponent error =
                    new TextComponent(
                            e.getMessage() == null ? e.getClass().getName() : e.getMessage());
            ClientCommands.sendError(
                    new TranslatableComponent("command.failed")
                            .withStyle(
                                    Style.EMPTY.withHoverEvent(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, error))));
            e.printStackTrace();
        }
    }

    public static void sendError(MutableComponent error) {
        sendFeedback(error.withStyle(ChatFormatting.RED));
    }

    public static void sendFeedback(MutableComponent message) {
        Minecraft.getInstance().gui.getChat().addMessage(message);
    }
}
