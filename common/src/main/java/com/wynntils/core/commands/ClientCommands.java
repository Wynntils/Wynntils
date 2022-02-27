/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.commands.TestWynntilsCommand;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.*;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

// Credits to Earthcomputer and Forge
// Parts of this code originates from https://github.com/Earthcomputer/clientcommands, and other parts originate
// from https://github.com/MinecraftForge/MinecraftForge
public class ClientCommands {

    static {
        ClientCommands.registerCommands();
    }

    private static CommandDispatcher<CommandSourceStack> clientSideCommands;

    public static CommandDispatcher<CommandSourceStack> getClientSideCommands() {
        return clientSideCommands;
    }

    public static void registerCommands() {
        clientSideCommands = new CommandDispatcher<>();
        new TestWynntilsCommand().register(clientSideCommands);
    }

    public static ClientCommandSourceStack getSource() {
        LocalPlayer player = McUtils.player();

        if (player == null) return null;

        return new ClientCommandSourceStack(player);
    }

    public static boolean executeCommand(StringReader reader, String command) {
        ClientCommandSourceStack source = getSource();

        if (source == null) return false;

        final ParseResults<CommandSourceStack> parse = clientSideCommands.parse(reader, source);

        if (!parse.getExceptions().isEmpty()) {
            return false; //let server handle command
        }

        try {
            clientSideCommands.execute(parse);
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

        return true;
    }

    private static void sendError(MutableComponent error) {
        McUtils.sendMessageToClient(error.withStyle(ChatFormatting.RED));
    }

}
