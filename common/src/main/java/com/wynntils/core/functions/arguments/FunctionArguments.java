/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions.arguments;

import com.wynntils.utils.type.ErrorOr;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// FIXME: Expose this to user and add i18n
public final class FunctionArguments {
    private final List<Argument> arguments;
    private final Map<String, Argument> lookupMap;

    private FunctionArguments(List<Argument> arguments) {
        this.arguments = arguments;

        this.lookupMap =
                this.arguments.stream().collect(Collectors.toMap(argument -> argument.name, argument -> argument));
    }

    public Argument getArgument(String name) {
        return this.lookupMap.get(name);
    }

    public static final class Builder {
        public static final Builder EMPTY = new Builder(List.of());

        private List<Argument> arguments;

        public Builder(List<Argument> arguments) {
            this.arguments = arguments;
        }

        public FunctionArguments buildWithDefaults() {
            return new FunctionArguments(this.arguments);
        }

        public ErrorOr<FunctionArguments> buildWithValues(List<String> values) {
            if (values.size() != this.arguments.size()) {
                return ErrorOr.error("Invalid number of arguments.");
            }

            for (int i = 0; i < this.arguments.size(); i++) {
                this.arguments.get(i).setValue(values.get(i));
            }

            return ErrorOr.of(new FunctionArguments(this.arguments));
        }
    }

    public static final class Argument<T> {
        private static final Map<Class<?>, Function<String, Object>> SUPPORTED_ARGUMENT_TYPES = Map.of(
                String.class, String::valueOf,
                Integer.class, Integer::parseInt,
                Double.class, Double::parseDouble,
                Boolean.class, Boolean::parseBoolean);

        private final String name;
        private final Class<T> type;
        private final T defaultValue;

        private T value;

        public Argument(String name, Class<T> type, T defaultValue) {
            if (!SUPPORTED_ARGUMENT_TYPES.containsKey(type)) {
                throw new IllegalArgumentException("Unsupported argument type: " + type);
            }

            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public void setValue(String value) {
            if (this.value != null) {
                throw new IllegalStateException("Tried setting argument value twice.");
            }

            this.value = (T) SUPPORTED_ARGUMENT_TYPES.get(this.type).apply(value);
        }

        public T getValue() {
            return this.value == null ? this.defaultValue : this.value;
        }
    }
}
