/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.wynntils.commands.BombBellCommand;
import com.wynntils.commands.CompassCommand;
import com.wynntils.commands.ConfigCommand;
import com.wynntils.commands.FeatureCommand;
import com.wynntils.commands.FunctionCommand;
import com.wynntils.commands.LocateCommand;
import com.wynntils.commands.LootrunCommand;
import com.wynntils.commands.QuestCommand;
import com.wynntils.commands.ServerCommand;
import com.wynntils.commands.TerritoryCommand;
import com.wynntils.commands.TokenCommand;
import com.wynntils.commands.UpdateCommand;
import com.wynntils.commands.WynntilsCommand;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

// Credits to Earthcomputer and Forge
// Parts of this code originates from https://github.com/Earthcomputer/clientcommands, and other
// parts originate from https://github.com/MinecraftForge/MinecraftForge
// Kudos to both of the above
public final class CommandManager extends Manager {
    private final Set<Command> commandInstanceSet = new HashSet<>();
    private final CommandDispatcher<CommandSourceStack> clientDispatcher = new CommandDispatcher<>();

    public CommandManager() {
        super(List.of());
        registerAllCommands();
    }

    public CommandDispatcher<CommandSourceStack> getClientDispatcher() {
        return clientDispatcher;
    }

    private void registerCommand(Command command) {
        commandInstanceSet.add(command);
        command.register(clientDispatcher);
    }

    private void registerCommandWithCommandSet(WynntilsCommand command) {
        command.registerWithCommands(clientDispatcher, commandInstanceSet);
        commandInstanceSet.add(command);
    }

    public boolean handleCommand(String message) {
        StringReader reader = new StringReader(message);
        return executeCommand(reader, message);
    }

    public CompletableFuture<Suggestions> getCompletionSuggestions(
            String cmd,
            CommandDispatcher<SharedSuggestionProvider> serverDispatcher,
            ParseResults<CommandSourceStack> clientParse,
            ParseResults<SharedSuggestionProvider> serverParse,
            int cursor) {
        StringReader stringReader = new StringReader(cmd);
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }

        CompletableFuture<Suggestions> clientSuggestions =
                clientDispatcher.getCompletionSuggestions(clientParse, cursor);
        CompletableFuture<Suggestions> serverSuggestions =
                serverDispatcher.getCompletionSuggestions(serverParse, cursor);

        CompletableFuture<Suggestions> result = new CompletableFuture<>();

        CompletableFuture.allOf(clientSuggestions, serverSuggestions).thenRun(() -> {
            final List<Suggestions> suggestions = new ArrayList<>();
            suggestions.add(clientSuggestions.join());
            suggestions.add(serverSuggestions.join());
            result.complete(Suggestions.merge(stringReader.getString(), suggestions));
        });

        return result;
    }

    public ClientCommandSourceStack getSource() {
        LocalPlayer player = McUtils.player();

        if (player == null) return null;

        return new ClientCommandSourceStack(player);
    }

    private boolean executeCommand(StringReader reader, String command) {
        ClientCommandSourceStack source = getSource();

        if (source == null) return false;

        final ParseResults<CommandSourceStack> parse = clientDispatcher.parse(reader, source);

        if (!parse.getExceptions().isEmpty()
                || (parse.getContext().getCommand() == null
                        && parse.getContext().getChild() == null)) {
            return false; // can't parse - let server handle command
        }

        try {
            clientDispatcher.execute(parse);
        } catch (CommandRuntimeException e) {
            sendError(Component.literal(e.getMessage()));
        } catch (CommandSyntaxException e) {
            sendError(Component.literal(e.getRawMessage().getString()));
            if (e.getInput() != null && e.getCursor() >= 0) {
                int cursor = Math.min(e.getCursor(), e.getInput().length());
                MutableComponent text = Component.literal("")
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.GRAY)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)));
                if (cursor > 10) text.append("...");

                text.append(e.getInput().substring(Math.max(0, cursor - 10), cursor));
                if (cursor < e.getInput().length()) {
                    text.append(Component.literal(e.getInput().substring(cursor))
                            .withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE));
                }

                text.append(Component.translatable("command.context.here")
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                sendError(text);
            }
        } catch (RuntimeException e) {
            MutableComponent error =
                    Component.literal(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
            sendError(Component.translatable("command.failed")
                    .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, error))));
            WynntilsMod.error("Failed to execute command.", e);
        }

        return true;
    }

    private void sendError(MutableComponent error) {
        McUtils.sendMessageToClient(error.withStyle(ChatFormatting.RED));
    }

    public Set<Command> getCommandInstanceSet() {
        return commandInstanceSet;
    }

    private void registerAllCommands() {
        registerCommand(new BombBellCommand());
        registerCommand(new CompassCommand());
        registerCommand(new ConfigCommand());
        registerCommand(new FeatureCommand());
        registerCommand(new FunctionCommand());
        registerCommand(new LocateCommand());
        registerCommand(new LootrunCommand());
        registerCommand(new QuestCommand());
        registerCommand(new UpdateCommand());
        registerCommand(new ServerCommand());
        registerCommand(new TerritoryCommand());
        registerCommand(new TokenCommand());

        // The WynntilsCommand must be registered last, since it
        // need the above commands as aliases
        registerCommandWithCommandSet(new WynntilsCommand());
    }
}
