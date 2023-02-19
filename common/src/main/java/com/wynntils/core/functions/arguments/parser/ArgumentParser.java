/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions.arguments.parser;

import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.utils.type.ErrorOr;
import java.util.Arrays;
import java.util.List;

public final class ArgumentParser {
    public static ErrorOr<FunctionArguments> parseArguments(
            FunctionArguments.Builder argumentsBuilder, String rawArgs) {
        if (rawArgs == null || rawArgs.isEmpty()) {
            return ErrorOr.of(argumentsBuilder.buildWithDefaults());
        }

        List<String> parts = Arrays.stream(rawArgs.split(";")).map(String::trim).toList();

        return argumentsBuilder.buildWithValues(parts);
    }
}
