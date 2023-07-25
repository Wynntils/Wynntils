/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.arguments.parser;

import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.expressions.parser.ExpressionParser;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ArgumentParser {
    public static ErrorOr<FunctionArguments> parseArguments(
            FunctionArguments.Builder argumentsBuilder, String rawArgs) {
        if (rawArgs == null || rawArgs.isEmpty()) {
            // 1, If there are no arguments, return early.
            if (argumentsBuilder.getArgumentCount() == 0) {
                return argumentsBuilder.buildWithValues(List.of());
            }

            // 2, If there are required arguments, return an error, otherwise return the default arguments.
            if (argumentsBuilder instanceof FunctionArguments.OptionalArgumentBuilder optionalArgumentBuilder) {
                return ErrorOr.of(optionalArgumentBuilder.buildWithDefaults());
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

        // 3, Calculate the expressions
        List<ErrorOr<Object>> calculatedExpressions =
                parts.stream().map(ErrorOr::getValue).map(Expression::calculate).toList();

        Optional<ErrorOr<Object>> optionalCalculationError =
                calculatedExpressions.stream().filter(ErrorOr::hasError).findFirst();

        // 4, If any of the expressions failed to calculate, return the error
        if (optionalCalculationError.isPresent()) {
            return ErrorOr.error(optionalCalculationError.get().getError());
        }

        // 5, Return the arguments as calculated expression values
        return argumentsBuilder.buildWithValues(
                calculatedExpressions.stream().map(ErrorOr::getValue).toList());
    }

    // This method handles splitting arguments in a "context-aware" way:
    //      This means that we need to handle nested expressions, indicated by parentheses
    //      The catch is that we need to handle strings with parantheses, which should not count as nesting
    //      Escaped string characters should also be handled
    // For example, the following string: "(test;test2;other_function(test4;test5))" should be parsed as ->
    // ["test;test2;other_function(test4;test5)"]
    private static List<String> splitArguments(String rawArgs) {
        List<String> arguments = new ArrayList<>();

        int paranthesesDepth = 0;
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
                    paranthesesDepth++;
                } else if (current == ')') {
                    paranthesesDepth--;
                } else if (current == ';' && paranthesesDepth == 0) {
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
