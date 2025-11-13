/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.arguments.parser;

import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.expressions.ConstantExpression;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.expressions.parser.ExpressionParser;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ArgumentParser {
    public static ErrorOr<List<Expression>> parseArguments(FunctionArguments.Builder argumentsBuilder, String rawArgs) {
        if (rawArgs == null || rawArgs.isEmpty()) {
            // 1, If there are no arguments, return early.
            if (argumentsBuilder.getArgumentCount() == 0) {
                return ErrorOr.of(List.of());
            }

            // 2, If there are required arguments, return an error, otherwise return the default arguments as constant
            // expressions.
            if (argumentsBuilder instanceof FunctionArguments.OptionalArgumentBuilder optionalArgumentBuilder) {
                return ErrorOr.of(optionalArgumentBuilder.getDefaults().stream()
                        .map(ConstantExpression::fromObject)
                        .toList());
            } else {
                return ErrorOr.error(
                        "Missing required arguments: (%s)".formatted(argumentsBuilder.getArgumentNamesString()));
            }
        }

        // 1, Split arguments and parse them as expressions
        List<ErrorOr<Expression>> parts = splitArguments(rawArgs).stream()
                .map(String::trim)
                .map(ExpressionParser::tryParse)
                .toList();

        Optional<ErrorOr<Expression>> optionalError =
                parts.stream().filter(ErrorOr::hasError).findFirst();

        // 2, If any of the expressions failed to parse, return the error
        if (optionalError.isPresent()) {
            return ErrorOr.error(optionalError.get().getError());
        }

        // 3, Return the arguments as expressions
        return ErrorOr.of(parts.stream().map(ErrorOr::getValue).toList());
    }

    // This method handles splitting arguments in a "context-aware" way:
    //      This means that we need to handle nested expressions, indicated by parentheses
    //      The catch is that we need to handle strings with parentheses, which should not count as nesting
    //      Escaped string characters should also be handled
    // For example, the following string: "(test;test2;other_function(test4;test5))" should be parsed as ->
    // ["test;test2;other_function(test4;test5)"]
    private static List<String> splitArguments(String rawArgs) {
        List<String> arguments = new ArrayList<>();

        int parenthesesDepth = 0;
        int processedIndex = 0;
        boolean inString = false;

        char previous = ' ';

        for (int i = 0; i < rawArgs.length(); i++) {
            char current = rawArgs.charAt(i);

            if (current == '"' && previous != '\\') {
                inString = !inString;
            } else {
                if (inString) continue;

                if (current == '(') {
                    parenthesesDepth++;
                } else if (current == ')') {
                    parenthesesDepth--;
                } else if (current == ';' && parenthesesDepth == 0) {
                    arguments.add(rawArgs.substring(processedIndex, i));
                    processedIndex = i + 1;
                }
            }

            previous = current;
        }

        arguments.add(rawArgs.substring(processedIndex));

        return arguments;
    }
}
