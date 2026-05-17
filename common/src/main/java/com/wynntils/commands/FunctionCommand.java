/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.core.text.StyledText;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class FunctionCommand extends Command {

    private static final SuggestionProvider<CommandSourceStack> FUNCTION_LIST_TYPES_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(ListType.values())
                            .map(Enum::name)
                            .map(s -> s.toLowerCase(Locale.ROOT))
                            .toList(),
                    builder);

    private static final Integer LIST_PAGE_LIMIT = 15;

    @Override
    public String getCommandName() {
        return "function";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext context) {
        return base;
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
//
//    private int listFunctions(CommandContext<CommandSourceStack> context) {
//        String type;
//
//        try {
//            type = context.getArgument("type", String.class);
//        } catch (Exception e) {
//            type = "all";
//        }
//
//        int page;
//
//        try {
//            page = context.getArgument("page", Integer.class);
//        } catch (Exception e) {
//            page = 1;
//        }
//
//        boolean all = type.equalsIgnoreCase("all");
//        boolean onlyGeneric = type.equalsIgnoreCase("generic");
//        boolean onlyNormal = type.equalsIgnoreCase("normal");
//
//        List<Function<?>> functions = Managers.Function.getFunctions().stream()
//                .filter(function -> all
//                        || (onlyNormal && !(function instanceof GenericFunction<?>))
//                        || (onlyGeneric && function instanceof GenericFunction<?>))
//                .sorted(Comparator.comparing(function -> function instanceof GenericFunction<?>)
//                        .thenComparing(o -> ((Function<?>) o).getName()))
//                .toList();
//
//        int totalPages = (int) Math.ceil((double) functions.size() / LIST_PAGE_LIMIT);
//        if (page > totalPages) {
//            page = totalPages;
//        }
//
//        List<Function<?>> paginatedFunctions;
//        int fromIndex = (page - 1) * LIST_PAGE_LIMIT;
//
//        if (functions.size() <= fromIndex) {
//            paginatedFunctions = functions;
//        } else {
//            paginatedFunctions = functions.subList(fromIndex, Math.min(fromIndex + LIST_PAGE_LIMIT, functions.size()));
//        }
//
//        MutableComponent response = Component.literal(
//                        onlyGeneric ? "Available generic functions: " : "Available functions:")
//                .withStyle(ChatFormatting.AQUA);
//
//        for (Function<?> function : paginatedFunctions) {
//            MutableComponent functionComponent = Component.literal("\n - ").withStyle(ChatFormatting.GRAY);
//
//            functionComponent
//                    .append(Component.literal(function.getName())
//                            .withStyle(
//                                    function instanceof GenericFunction<?>
//                                            ? ChatFormatting.GOLD
//                                            : ChatFormatting.YELLOW))
//                    .withStyle(style -> style.withClickEvent(
//                            new ClickEvent.SuggestCommand("/function help " + function.getName())));
//            if (!function.getAliasList().isEmpty()) {
//                String aliasList = String.join(", ", function.getAliasList());
//
//                functionComponent
//                        .append(Component.literal(" [alias: ").withStyle(ChatFormatting.GRAY))
//                        .append(Component.literal(aliasList).withStyle(ChatFormatting.WHITE))
//                        .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
//            }
//
//            functionComponent.withStyle(style ->
//                    style.withHoverEvent(new HoverEvent.ShowText(Component.literal(function.getDescription()))));
//
//            response.append(functionComponent);
//        }
//
//        int previousPage = page == 1 ? totalPages : page - 1;
//        int nextPage = page == totalPages ? 1 : page + 1;
//        String previousPageCommand = "/function list " + type + " " + previousPage;
//        String nextPageCommand = "/function list " + type + " " + nextPage;
//
//        MutableComponent pageComponent = Component.literal("\n")
//                .append(Component.literal("< Previous")
//                        .withStyle(ChatFormatting.DARK_AQUA)
//                        .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand(previousPageCommand)))
//                        .withStyle(style -> style.withHoverEvent(
//                                new HoverEvent.ShowText(Component.literal("Go to previous page")))))
//                .append(Component.literal(" (" + page + "/" + totalPages + ") ").withStyle(ChatFormatting.AQUA))
//                .append(Component.literal("Next >")
//                        .withStyle(ChatFormatting.DARK_AQUA)
//                        .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand(nextPageCommand)))
//                        .withStyle(style ->
//                                style.withHoverEvent(new HoverEvent.ShowText(Component.literal("Go to next page")))));
//
//        response.append(pageComponent);
//
//        context.getSource().sendSuccess(() -> response, false);
//
//        return 1;
//    }
//
//    private int enableFunction(CommandContext<CommandSourceStack> context) {
//        String functionName = context.getArgument("function", String.class);
//
//        Optional<Function<?>> functionOptional = Managers.Function.forName(functionName);
//
//        if (functionOptional.isEmpty()) {
//            context.getSource()
//                    .sendFailure(Component.literal("Function not found!").withStyle(ChatFormatting.RED));
//            return 0;
//        }
//
//        Function<?> function = functionOptional.get();
//        if (!Managers.Function.isCrashed(function)) {
//            context.getSource()
//                    .sendFailure(Component.literal("Function does not need to be enabled")
//                            .withStyle(ChatFormatting.RED));
//            return 0;
//        }
//
//        Managers.Function.enableFunction(function);
//
//        Component response = Component.literal(function.getName())
//                .withStyle(ChatFormatting.AQUA)
//                .append(Component.literal(" is now enabled").withStyle(ChatFormatting.WHITE));
//        context.getSource().sendSuccess(() -> response, false);
//        return 1;
//    }
//
//    private int getValue(CommandContext<CommandSourceStack> context) {
//        Component argument;
//        try {
//            argument = Component.literal(StringArgumentType.getString(context, "argument"));
//        } catch (IllegalArgumentException e) {
//            argument = Component.literal("");
//        }
//
//        String functionName = context.getArgument("function", String.class);
//        Optional<Function<?>> functionOptional = Managers.Function.forName(functionName);
//
//        if (functionOptional.isEmpty()) {
//            context.getSource()
//                    .sendFailure(Component.literal("Function not found").withStyle(ChatFormatting.RED));
//            return 0;
//        }
//        Function<?> function = functionOptional.get();
//
//        MutableComponent result = Component.literal("");
//        result.append(
//                Managers.Function.getSimpleValueString(function, argument.getString(), ChatFormatting.YELLOW, true));
//        context.getSource().sendSuccess(() -> result, false);
//        return 1;
//    }
//
//    private int helpForFunction(CommandContext<CommandSourceStack> context) {
//        String functionName = context.getArgument("function", String.class);
//
//        Optional<Function<?>> functionOptional = Managers.Function.forName(functionName);
//
//        if (functionOptional.isEmpty()) {
//            context.getSource()
//                    .sendFailure(Component.literal("Function not found").withStyle(ChatFormatting.RED));
//            return 0;
//        }
//
//        Function<?> function = functionOptional.get();
//
//        MutableComponent helpComponent = Component.empty();
//
//        boolean isArgumentOptional =
//                function.getArgumentsBuilder() instanceof FunctionArguments.OptionalArgumentBuilder;
//
//        helpComponent.append(ChatFormatting.GRAY + "Type: " + ChatFormatting.WHITE
//                + (function instanceof GenericFunction<?> ? "Generic" : "Normal") + "\n");
//        helpComponent.append(
//                ChatFormatting.GRAY + "Description: " + ChatFormatting.WHITE + function.getDescription() + "\n");
//        helpComponent.append(ChatFormatting.GRAY + "Aliases:" + ChatFormatting.WHITE + " ["
//                + String.join(", ", function.getAliasList()) + "]\n");
//        helpComponent.append(
//                ChatFormatting.GRAY + "Returns: " + ChatFormatting.WHITE + function.getReturnTypeName() + "\n");
//        helpComponent.append(ChatFormatting.GRAY + "Arguments:" + ChatFormatting.WHITE + " ("
//                + (isArgumentOptional ? "Optional" : "Required")
//                + ")");
//
//        for (Argument<?> argument : function.getArgumentsBuilder().getArguments()) {
//            String type;
//
//            if (isArgumentOptional) {
//                type = "(%s, default: %s)"
//                        .formatted(
//                                argument.getType().getSimpleName(),
//                                argument.getDefaultValue().toString());
//            } else if (argument instanceof ListArgument<?>) {
//                type = "(List<" + argument.getType().getSimpleName() + ">)";
//            } else {
//                type = ("(" + argument.getType().getSimpleName() + ")");
//            }
//
//            String argumentDescription = "\n - " + ChatFormatting.YELLOW + argument.getName() + " " + type
//                    + ChatFormatting.WHITE + ": " + function.getArgumentDescription(argument.getName());
//
//            helpComponent.append(argumentDescription);
//        }
//
//        Component response = Component.literal("Function Manual: ")
//                .withStyle(ChatFormatting.AQUA)
//                .append(Component.literal(function.getName() + "\n").withStyle(ChatFormatting.BOLD))
//                .append(helpComponent.withStyle(ChatFormatting.WHITE));
//
//        context.getSource().sendSuccess(() -> response, false);
//        return 1;
//    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }

    private enum ListType {
        ALL,
        GENERIC,
        NORMAL
    }
}
