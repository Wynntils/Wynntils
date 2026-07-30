/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.core.text.StyledText;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import com.wynntils.templates.functions.FunctionDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class FunctionCommand extends Command {

    private static final SuggestionProvider<CommandSourceStack> FUNCTION_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Managers.Function.getFunctions().stream().map(FunctionDefinition::name),
                    builder);
    private static final Integer LIST_PAGE_LIMIT = 15;

    @Override
    public String getCommandName() {
        return "function";
    }

    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext context) {
        return base.then(Commands.literal("list")
                        .executes(this::listFunctions)
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(this::listFunctions)))
//                .then(Commands.literal("enable")
//                        .then(Commands.argument("function", StringArgumentType.word())
//                                .suggests(CRASHED_FUNCTION_SUGGESTION_PROVIDER)
//                                .executes(this::enableFunction)))
                .then(Commands.literal("get")
                        .then(Commands.argument("function", StringArgumentType.word())
                                .suggests(FUNCTION_SUGGESTION_PROVIDER)
                                .executes(this::getValue)
                                .then(Commands.argument("argument", StringArgumentType.greedyString())
                                        .executes(this::getValue))))
                .then(Commands.literal("help")
                        .then(Commands.argument("function", StringArgumentType.word())
                                .suggests(FUNCTION_SUGGESTION_PROVIDER)
                                .executes(this::helpForFunction)))
                .then(Commands.literal("test")
                        .then(Commands.argument("template", StringArgumentType.greedyString())
                                .executes(this::testExpression))
                        .build())
                .executes(this::syntaxError);
    }

    private int testExpression(CommandContext<CommandSourceStack> context) {
        String template = context.getArgument("template", String.class);

        StyledText[] result = Managers.Function.doFormatLines(template);

        StyledText resultString = StyledText.join(", ", result);

        context.getSource()
                .sendSuccess(
                        () -> Component.literal("Template calculated: \"%s§r\" -> [%s§r]"
                                .formatted(template, resultString.getString())),
                        false);

        return 1;
    }

        private int listFunctions(CommandContext<CommandSourceStack> context) {
            int page;

            try {
                page = context.getArgument("page", Integer.class);
            } catch (Exception e) {
                page = 1;
            }

            List<FunctionDefinition> functions = Managers.Function.getFunctions().stream()
                    .filter(fun -> !fun.isAlias()).toList();

            int totalPages = (int) Math.ceil((double) functions.size() / LIST_PAGE_LIMIT);
            if (page > totalPages) {
                page = totalPages;
            }

            List<FunctionDefinition> paginatedFunctions;
            int fromIndex = (page - 1) * LIST_PAGE_LIMIT;

            if (functions.size() <= fromIndex) {
                paginatedFunctions = functions;
            } else {
                paginatedFunctions = functions.subList(fromIndex, Math.min(fromIndex + LIST_PAGE_LIMIT, functions.size()));
            }

            MutableComponent response = Component.literal("Available functions:")
                    .withStyle(ChatFormatting.AQUA);

            for (FunctionDefinition function: paginatedFunctions) {
                MutableComponent functionComponent = Component.literal("\n - ").withStyle(ChatFormatting.GRAY);

                functionComponent
                        .append(Component.literal(function.name())
                                .withStyle(ChatFormatting.YELLOW))
                        .withStyle(style -> style.withClickEvent(
                                new ClickEvent.SuggestCommand("/function help " + function.name())));
                if (function.aliases().length != 0) {
                    String aliasList = String.join(", ", function.aliases());

                    functionComponent
                            .append(Component.literal(" [alias: ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(aliasList).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
                }

                functionComponent.withStyle(style ->
                        style.withHoverEvent(new HoverEvent.ShowText(Component.literal(Managers.Function.getFunctionDescription(function)))));

                response.append(functionComponent);
            }

            int previousPage = page == 1 ? totalPages : page - 1;
            int nextPage = page == totalPages ? 1 : page + 1;
            String previousPageCommand = "/function list " + previousPage;
            String nextPageCommand = "/function list " + nextPage;

            MutableComponent pageComponent = Component.literal("\n")
                    .append(Component.literal("< Previous")
                            .withStyle(ChatFormatting.DARK_AQUA)
                            .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand(previousPageCommand)))
                            .withStyle(style -> style.withHoverEvent(
                                    new HoverEvent.ShowText(Component.literal("Go to previous page")))))
                    .append(Component.literal(" (" + page + "/" + totalPages + ") ").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal("Next >")
                            .withStyle(ChatFormatting.DARK_AQUA)
                            .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand(nextPageCommand)))
                            .withStyle(style ->
                                    style.withHoverEvent(new HoverEvent.ShowText(Component.literal("Go to next page")))));

            response.append(pageComponent);

            context.getSource().sendSuccess(() -> response, false);

            return 1;
        }

        private int getValue(CommandContext<CommandSourceStack> context) {
            Component argument;
            try {
                argument = Component.literal(StringArgumentType.getString(context, "argument"));
            } catch (IllegalArgumentException e) {
                argument = Component.literal("");
            }

            String functionName = context.getArgument("function", String.class);
            Optional<FunctionDefinition> functionOptional = Managers.Function.forName(functionName);

            if (functionOptional.isEmpty()) {
                context.getSource()
                        .sendFailure(Component.literal("Function not found").withStyle(ChatFormatting.RED));
                return 0;
            }
            FunctionDefinition function = functionOptional.get();

            MutableComponent result = Component.literal("");

            try {
                result.append(function.method().invoke(null, argument.getString()).toString());
            } catch (Exception e) {
                context.getSource().sendFailure(Component.literal(e.getMessage()));
                return 0;
            }

            context.getSource().sendSuccess(() -> result, false);
            return 1;
        }

        private int helpForFunction(CommandContext<CommandSourceStack> context) {
            String functionName = context.getArgument("function", String.class);

            Optional<FunctionDefinition> functionOptional = Managers.Function.forName(functionName);

            if (functionOptional.isEmpty()) {
                context.getSource()
                        .sendFailure(Component.literal("Function not found").withStyle(ChatFormatting.RED));
                return 0;
            }

            FunctionDefinition function = functionOptional.get();

            MutableComponent helpComponent = Component.empty();

            helpComponent.append(
                    ChatFormatting.GRAY + "Description: " + ChatFormatting.WHITE + Managers.Function.getFunctionDescription(function) + "\n");
            helpComponent.append(ChatFormatting.GRAY + "Aliases:" + ChatFormatting.WHITE + " ["
                    + String.join(", ", function.aliases()) + "]\n");
            helpComponent.append(
                    ChatFormatting.GRAY + "Returns: " + ChatFormatting.WHITE + function.returnType().getSimpleName() + "\n");

            for (int i = 0; i < function.parameterTypes().length; i++) {
                Class<?> parameter = function.parameterTypes()[i];
                String name = function.getParameterName(i);

                String type;

                if (function.isVarArgs()) {
                    type = "(List<" + parameter.getSimpleName() + ">)";
                } else {
                    type = ("(" + parameter.getSimpleName() + ")");
                }

                String argumentDescription = "\n - " + ChatFormatting.YELLOW + name + " " + type
                        + ChatFormatting.WHITE + ": " + Managers.Function.getFunctionParameterDescription(function, name);

                helpComponent.append(argumentDescription);
            }

            Component response = Component.literal("Function Manual: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(function.name() + "\n").withStyle(ChatFormatting.BOLD))
                    .append(helpComponent.withStyle(ChatFormatting.WHITE));

            context.getSource().sendSuccess(() -> response, false);
            return 1;
        }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }
}
