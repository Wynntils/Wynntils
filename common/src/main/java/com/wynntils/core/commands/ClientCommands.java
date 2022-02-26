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

// Credits to Earthcomputer and Forge as respectively
// Parts of this code originates from https://github.com/Earthcomputer/clientcommands, and other parts originate
// from https://github.com/MinecraftForge/MinecraftForge
public class ClientCommands {

    private static CommandDispatcher<CommandSourceStack> clientSideCommands;

    public static CommandDispatcher<CommandSourceStack> getClientSideCommands() {
        return clientSideCommands;
    }

    public static CommandDispatcher<SharedSuggestionProvider> loadCommands(CommandDispatcher<SharedSuggestionProvider> serverCommands) {
        CommandDispatcher<SharedSuggestionProvider> newServerCommands = new CommandDispatcher<>();
        copy(newServerCommands.getRoot(), serverCommands.getRoot());

        clientSideCommands = new CommandDispatcher<>();
        new TestWynntilsCommand().register(clientSideCommands);

        // Add suggestions
        mergeCommandNode(clientSideCommands.getRoot(), newServerCommands.getRoot(), new IdentityHashMap<>(), getSource(), (context) -> 0, (suggestions) -> {
            @SuppressWarnings("unchecked")
            SuggestionProvider<SharedSuggestionProvider> suggestionProvider = SuggestionProviders
                    .safelySwap((SuggestionProvider<SharedSuggestionProvider>) (SuggestionProvider<?>) suggestions);
            if (suggestionProvider == SuggestionProviders.ASK_SERVER) {
                suggestionProvider = (context, builder) -> {
                    ClientCommandSourceStack source = getSource();
                    StringReader reader = new StringReader(context.getInput());
                    if (reader.canRead() && reader.peek() == '/')
                    {
                        reader.skip();
                    }

                    ParseResults<CommandSourceStack> parse = clientSideCommands.parse(reader, source);
                    return clientSideCommands.getCompletionSuggestions(parse);
                };
            }
            return suggestionProvider;
        });

        return newServerCommands;
    }

    private static ClientCommandSourceStack getSource() {
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

    /**
     *
     * Creates a deep copy of the sourceNode while keeping the redirects referring to the old command tree
     *
     * @param sourceNode
     *            the original
     * @param resultNode
     *            the result
     */
    private static <S> void copy(CommandNode<S> sourceNode, CommandNode<S> resultNode)
    {
        Map<CommandNode<S>, CommandNode<S>> newNodes = new IdentityHashMap<>();
        newNodes.put(sourceNode, resultNode);
        for (CommandNode<S> child : sourceNode.getChildren())
        {
            CommandNode<S> copy = newNodes.computeIfAbsent(child, innerChild ->
            {
                ArgumentBuilder<S, ?> builder = innerChild.createBuilder();
                CommandNode<S> innerCopy = builder.build();
                copy(innerChild, innerCopy);
                return innerCopy;
            });
            resultNode.addChild(copy);
        }
    }

    private static <S, T> void mergeCommandNode(CommandNode<S> sourceNode, CommandNode<T> resultNode, Map<CommandNode<S>, CommandNode<T>> sourceToResult,
                                                S canUse, Command<T> execute, Function<SuggestionProvider<S>, SuggestionProvider<T>> sourceToResultSuggestion) {
        sourceToResult.put(sourceNode, resultNode);
        for (CommandNode<S> sourceChild : sourceNode.getChildren())
        {
            if (sourceChild.canUse(canUse))
            {
                resultNode.addChild(toResult(sourceChild, sourceToResult, canUse, execute, sourceToResultSuggestion));
            }
        }
    }

    private static <S, T> CommandNode<T> toResult(CommandNode<S> sourceNode, Map<CommandNode<S>, CommandNode<T>> sourceToResult, S canUse, Command<T> execute,
                                                  Function<SuggestionProvider<S>, SuggestionProvider<T>> sourceToResultSuggestion) {
        if (sourceToResult.containsKey(sourceNode))
            return sourceToResult.get(sourceNode);

        ArgumentBuilder<T, ?> resultBuilder;
        if (sourceNode instanceof ArgumentCommandNode<?, ?>)
        {
            ArgumentCommandNode<S, ?> sourceArgument = (ArgumentCommandNode<S, ?>) sourceNode;
            RequiredArgumentBuilder<T, ?> resultArgumentBuilder = RequiredArgumentBuilder.argument(sourceArgument.getName(), sourceArgument.getType());
            if (sourceArgument.getCustomSuggestions() != null)
            {
                resultArgumentBuilder.suggests(sourceToResultSuggestion.apply(sourceArgument.getCustomSuggestions()));
            }
            resultBuilder = resultArgumentBuilder;
        }
        else if (sourceNode instanceof LiteralCommandNode<?>)
        {
            LiteralCommandNode<S> sourceLiteral = (LiteralCommandNode<S>) sourceNode;
            resultBuilder = LiteralArgumentBuilder.literal(sourceLiteral.getLiteral());
        }
        else if (sourceNode instanceof RootCommandNode<?>)
        {
            CommandNode<T> resultNode = new RootCommandNode<>();
            mergeCommandNode(sourceNode, resultNode, sourceToResult, canUse, execute, sourceToResultSuggestion);
            return resultNode;
        }
        else
        {
            throw new IllegalStateException("Node type " + sourceNode + " is not a standard node type");
        }

        if (sourceNode.getCommand() != null)
        {
            resultBuilder.executes(execute);
        }

        if (sourceNode.getRedirect() != null)
        {
            resultBuilder.redirect(toResult(sourceNode.getRedirect(), sourceToResult, canUse, execute, sourceToResultSuggestion));
        }

        CommandNode<T> resultNode = resultBuilder.build();
        mergeCommandNode(sourceNode, resultNode, sourceToResult, canUse, execute, sourceToResultSuggestion);
        return resultNode;
    }
}
