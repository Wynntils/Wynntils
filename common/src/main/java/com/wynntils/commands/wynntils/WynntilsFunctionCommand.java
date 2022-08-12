/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands.wynntils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.FunctionRegistry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public final class WynntilsFunctionCommand {
    private static final SuggestionProvider<CommandSourceStack> userFunctionSuggestionProvider =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    FunctionRegistry.getFunctions().stream().map(Function::getName), builder);

    public static LiteralCommandNode<CommandSourceStack> buildListNode() {
        return Commands.literal("list")
                .executes(WynntilsFunctionCommand::listFunctions)
                .build();
    }

    private static int listFunctions(CommandContext<CommandSourceStack> context) {
        Set<Function> functions = FunctionRegistry.getFunctions().stream().collect(Collectors.toUnmodifiableSet());

        MutableComponent response = new TextComponent("Currently registered functions:").withStyle(ChatFormatting.AQUA);

        for (Function function : functions) {
            response.append(new TextComponent("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(function.getName()).withStyle(ChatFormatting.YELLOW));
        }

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    public static LiteralCommandNode<CommandSourceStack> buildGetValueNode() {
        return Commands.literal("get")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(userFunctionSuggestionProvider)
                        .executes(WynntilsFunctionCommand::getValue))
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> buildGetValueWithArgumentNode() {
        return Commands.literal("get")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(userFunctionSuggestionProvider)
                        .then(Commands.argument("argument", StringArgumentType.greedyString())
                                .executes(WynntilsFunctionCommand::getValue)))
                .build();
    }

    private static int getValue(CommandContext<CommandSourceStack> context) {
        Component argument;
        try{
            argument = new TextComponent(StringArgumentType.getString(context, "argument"));
        } catch (IllegalArgumentException e) {
            argument = new TextComponent("");
        }

        String functionName = context.getArgument("function", String.class);
        Optional<Function> functionOptional = FunctionRegistry.forName(functionName);

        if (functionOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Function not found!").withStyle(ChatFormatting.RED));
            return 0;
        }
        Function function = functionOptional.get();

        String value = function.getValue(argument.getString());

        Component response = new TextComponent(function.getName() + " returns ")
                .withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(value).withStyle(ChatFormatting.YELLOW));
        context.getSource().sendSuccess(response, false);
        return 1;
    }

    public static LiteralCommandNode<CommandSourceStack> buildHelpNode() {
        return Commands.literal("help")
                .then(Commands.argument("function", StringArgumentType.word())
                        .suggests(userFunctionSuggestionProvider)
                        .executes(WynntilsFunctionCommand::helpForFunction))
                .build();
    }

    private static int helpForFunction(CommandContext<CommandSourceStack> context) {
        String functionName = context.getArgument("function", String.class);

        Optional<Function> functionOptional = FunctionRegistry.forName(functionName);

        if (functionOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Function not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Function function = functionOptional.get();

        String helpText = function.getDescription();

        Component response = new TextComponent(function.getName() + ": ")
                .withStyle(ChatFormatting.AQUA)
                .append(new TextComponent(helpText).withStyle(ChatFormatting.WHITE));
        context.getSource().sendSuccess(response, false);
        return 1;
    }
}
