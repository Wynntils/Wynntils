/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.commands.Command;
import com.wynntils.core.components.Managers;
import com.wynntils.core.functions.Function;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class FunctionCommand extends Command {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("function")
                .then(this.buildListNode())
                .then(this.buildEnableNode())
                .then(this.buildGetValueNode())
                .then(this.buildGetValueWithArgumentNode())
                .then(this.buildHelpNode())
                .executes(this::syntaxError);
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }

    private final SuggestionProvider<CommandSourceStack> functionSuggestionProvider =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Managers.Function.getFunctions().stream().map(Function::getName), builder);

    private final SuggestionProvider<CommandSourceStack> crashedFunctionSuggestionProvider =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Managers.Function.getFunctions().stream()
                            .filter(Managers.Function::isCrashed)
                            .map(Function::getName),
                    builder);

    private LiteralCommandNode<CommandSourceStack> buildListNode() {
        return Commands.literal("list").executes(this::listFunctions).build();
    }

    private int listFunctions(CommandContext<CommandSourceStack> context) {
        List<Function<?>> functions = Managers.Function.getFunctions().stream()
                .sorted(Comparator.comparing(Function::getName))
                .toList();

        MutableComponent response = Component.literal("Available functions:").withStyle(ChatFormatting.AQUA);

        for (Function<?> function : functions) {
            MutableComponent functionComponent = Component.literal("\n - ").withStyle(ChatFormatting.GRAY);

            functionComponent.append(Component.literal(function.getName()).withStyle(ChatFormatting.YELLOW));
            if (!function.getAliases().isEmpty()) {
                String aliasList = String.join(", ", function.getAliases());

                functionComponent
                        .append(Component.literal(" [alias: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(aliasList).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
            }

            functionComponent.withStyle(style -> style.withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(function.getDescription()))));

            response.append(functionComponent);
        }

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    private LiteralCommandNode<CommandSourceStack> buildEnableNode() {
        return Commands.literal("enable")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(crashedFunctionSuggestionProvider)
                        .executes(this::enableFunction))
                .build();
    }

    private int enableFunction(CommandContext<CommandSourceStack> context) {
        String functionName = context.getArgument("function", String.class);

        Optional<Function<?>> functionOptional = Managers.Function.forName(functionName);

        if (functionOptional.isEmpty()) {
            context.getSource()
                    .sendFailure(Component.literal("Function not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Function<?> function = functionOptional.get();
        if (!Managers.Function.isCrashed(function)) {
            context.getSource()
                    .sendFailure(Component.literal("Function does not need to be enabled")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        Managers.Function.enableFunction(function);

        Component response = Component.literal(function.getName())
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" is now enabled").withStyle(ChatFormatting.WHITE));
        context.getSource().sendSuccess(response, false);
        return 1;
    }

    private LiteralCommandNode<CommandSourceStack> buildGetValueNode() {
        return Commands.literal("get")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(functionSuggestionProvider)
                        .executes(this::getValue))
                .build();
    }

    private LiteralCommandNode<CommandSourceStack> buildGetValueWithArgumentNode() {
        return Commands.literal("get")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(functionSuggestionProvider)
                        .then(Commands.argument("argument", StringArgumentType.greedyString())
                                .executes(this::getValue)))
                .build();
    }

    private int getValue(CommandContext<CommandSourceStack> context) {
        Component argument;
        try {
            argument = Component.literal(StringArgumentType.getString(context, "argument"));
        } catch (IllegalArgumentException e) {
            argument = Component.literal("");
        }

        String functionName = context.getArgument("function", String.class);
        Optional<Function<?>> functionOptional = Managers.Function.forName(functionName);

        if (functionOptional.isEmpty()) {
            context.getSource()
                    .sendFailure(Component.literal("Function not found").withStyle(ChatFormatting.RED));
            return 0;
        }
        Function<?> function = functionOptional.get();

        MutableComponent result = Component.literal("");
        result.append(
                Managers.Function.getSimpleValueString(function, argument.getString(), ChatFormatting.YELLOW, true));
        context.getSource().sendSuccess(result, false);
        return 1;
    }

    private LiteralCommandNode<CommandSourceStack> buildHelpNode() {
        return Commands.literal("help")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(functionSuggestionProvider)
                        .executes(this::helpForFunction))
                .build();
    }

    private int helpForFunction(CommandContext<CommandSourceStack> context) {
        String functionName = context.getArgument("function", String.class);

        Optional<Function<?>> functionOptional = Managers.Function.forName(functionName);

        if (functionOptional.isEmpty()) {
            context.getSource()
                    .sendFailure(Component.literal("Function not found").withStyle(ChatFormatting.RED));
            return 0;
        }

        Function<?> function = functionOptional.get();

        String helpText = function.getDescription();

        Component response = Component.literal(function.getName() + ": ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(helpText).withStyle(ChatFormatting.WHITE));
        context.getSource().sendSuccess(response, false);
        return 1;
    }
}
