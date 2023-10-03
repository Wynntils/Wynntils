/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.expressions;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.arguments.parser.ArgumentParser;
import com.wynntils.utils.type.ErrorOr;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FunctionExpression extends Expression {
    // Function format:
    //   function_name(argument1; argument2; ...)
    //   function_name()
    //   function_name
    // Format appendix: F - formatted, 2 - decimal precision
    //   :F2
    //   :2
    //   :F
    private static final Pattern FUNCTION_EXPRESSION_PATTERN = Pattern.compile(
            "(?<function>[a-zA-Z_]+)(\\((?<argument>.*)\\))?(\\:(?<formatted>F)?(?<decimals>[0-9]+)?)?",
            Pattern.DOTALL);

    private final Function<?> function;
    private final List<Expression> argumentExpressions;
    private final boolean formatted;
    private final int decimals;

    private FunctionExpression(
            String rawExpression,
            Function<?> function,
            List<Expression> argumentExpressions,
            boolean formatted,
            int decimals) {
        super(rawExpression);
        this.function = function;
        this.argumentExpressions = argumentExpressions;

        this.formatted = formatted;
        this.decimals = decimals;
    }

    @Override
    public ErrorOr<Object> calculate() {
        ErrorOr<FunctionArguments> arguments = getArguments();
        if (arguments.hasError()) {
            return ErrorOr.error(arguments.getError());
        }

        return Managers.Function.getRawFunctionValue(function, arguments.getValue());
    }

    @Override
    public ErrorOr<String> calculateFormattedString() {
        ErrorOr<FunctionArguments> arguments = getArguments();
        if (arguments.hasError()) {
            return ErrorOr.error(arguments.getError());
        }

        return ErrorOr.of(
                Managers.Function.getStringFunctionValue(function, arguments.getValue(), formatted, decimals));
    }

    private ErrorOr<FunctionArguments> getArguments() {
        List<ErrorOr<Object>> calculatedExpressions =
                argumentExpressions.stream().map(Expression::calculate).toList();

        Optional<ErrorOr<Object>> firstError =
                calculatedExpressions.stream().filter(ErrorOr::hasError).findFirst();
        if (firstError.isPresent()) {
            return ErrorOr.error(firstError.get().getError());
        }

        return function.getArgumentsBuilder()
                .buildWithValues(
                        calculatedExpressions.stream().map(ErrorOr::getValue).toList());
    }

    // This method attempts to parse a function expression in the following ways:
    //   1. The expression is not a function expression, in which case it returns an empty optional.
    //   2. The expression could be a function expression, but the function name is not a valid function, in which case
    //      it returns an empty optional.
    //   3. The expression is a function expression, and the function name is a valid function, but the arguments are
    //      invalid, in which case it returns an error.
    //   4. The expression is a function expression, and the function name is a valid function, and the arguments are
    //      valid, in which case it returns the parsed expression.
    //
    //   Arguments are valid if:
    //     1. The number of arguments is equal to the number of arguments the function expects.
    //     2. There is no argument part in the expression, in which case the function is called with the default
    //        arguments.
    //     3. The argument part is empty, in which case the function is called with the default arguments.
    //
    //   Formatting parsing:
    //     1. If the expression does not contain a formatting appendix, the expression is not formatted and the decimal
    //        count is set to a default of 2.
    //     2. If the expression contains a formatting appendix, the expression is formatted and the decimal count is set
    //        to the value in the appendix.
    //        2.1. The appendix can contain only a decimal count, only a formatting flag, or both.
    //        2.2. The formatting flag needs to be a capital F.
    //        2.3. The decimal count needs to be an integer of any length.
    //        2.4. The decimal count can be omitted, in which case it is set to a default of 2.
    //        2.5. The formatting flag can be omitted, in which case the expression is not formatted.

    public static ErrorOr<Optional<Expression>> tryParse(String rawExpression) {
        Matcher matcher = FUNCTION_EXPRESSION_PATTERN.matcher(rawExpression);

        if (!matcher.matches()) return ErrorOr.of(Optional.empty());

        // Handle function parsing

        Optional<Function<?>> functionOptional = Managers.Function.forName(matcher.group("function"));

        if (functionOptional.isEmpty()) {
            return ErrorOr.of(Optional.empty());
        }

        Function<?> function = functionOptional.get();

        // Handle formatting parsing

        boolean isFormatted = matcher.group("formatted") != null;
        String decimalMatch = matcher.group("decimals");
        int decimals = decimalMatch != null ? Integer.parseInt(decimalMatch) : 2;

        // Handle argument parsing

        String rawArguments = matcher.group("argument");

        ErrorOr<List<Expression>> argumentExpressions =
                ArgumentParser.parseArguments(function.getArgumentsBuilder(), rawArguments);

        return argumentExpressions.hasError()
                ? ErrorOr.error(argumentExpressions.getError())
                : ErrorOr.of(Optional.of(new FunctionExpression(
                        rawExpression, function, argumentExpressions.getValue(), isFormatted, decimals)));
    }
}
