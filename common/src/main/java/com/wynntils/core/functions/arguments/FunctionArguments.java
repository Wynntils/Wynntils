/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions.arguments;

import com.wynntils.utils.type.ErrorOr;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public abstract static class Builder {
        protected List<Argument> arguments;

        protected Builder(List<Argument> arguments) {
            this.arguments = arguments;
        }

        public ErrorOr<FunctionArguments> buildWithValues(List<Object> values) {
            if (values.size() != this.arguments.size()) {
                return ErrorOr.error("Invalid number of arguments");
            }

            for (int i = 0; i < this.arguments.size(); i++) {
                Argument argument = this.arguments.get(i);

                if (!argument.getType().isAssignableFrom(values.get(i).getClass())) {
                    return ErrorOr.error("Invalid argument type: \"%s\" is not a %s"
                            .formatted(
                                    values.get(i).toString(), argument.getType().getSimpleName()));
                }

                argument.setValue(values.get(i));
            }

            return ErrorOr.of(new FunctionArguments(this.arguments));
        }

        public String getArgumentNamesString() {
            return arguments.stream().map(Argument::getName).collect(Collectors.joining("; "));
        }

        public List<Argument> getArguments() {
            return Collections.unmodifiableList(arguments);
        }

        public int getArgumentCount() {
            return arguments.size();
        }
    }

    // A builder for functions with required arguments
    public static class RequiredArgumentBuilder extends Builder {
        public static final Builder EMPTY = new RequiredArgumentBuilder(List.of());

        public RequiredArgumentBuilder(List<Argument> arguments) {
            super(arguments);
        }
    }

    // A builder for functions with optional arguments
    public static class OptionalArgumentBuilder extends Builder {
        public static final Builder EMPTY = new OptionalArgumentBuilder(List.of());

        public OptionalArgumentBuilder(List<Argument> arguments) {
            super(arguments);
        }

        public FunctionArguments buildWithDefaults() {
            return new FunctionArguments(this.arguments);
        }
    }

    public static final class Argument<T> {
        private static final List<Class<?>> SUPPORTED_ARGUMENT_TYPES =
                List.of(String.class, Integer.class, Double.class, Number.class, Boolean.class);

        private final String name;
        private final Class<T> type;
        private final T defaultValue;

        private T value;

        public Argument(String name, Class<T> type, T defaultValue) {
            if (!SUPPORTED_ARGUMENT_TYPES.contains(type)) {
                throw new IllegalArgumentException("Unsupported argument type: " + type);
            }

            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public void setValue(Object value) {
            if (this.value != null) {
                throw new IllegalStateException("Tried setting argument value twice.");
            }

            this.value = (T) value;
        }

        public String getName() {
            return name;
        }

        public Class<T> getType() {
            return type;
        }

        public T getValue() {
            return this.value == null ? this.defaultValue : this.value;
        }

        public T getDefaultValue() {
            return this.defaultValue;
        }

        public Boolean getBooleanValue() {
            return (Boolean) this.getValue();
        }

        public Integer getIntegerValue() {
            if (this.type == Number.class) {
                return ((Number) this.getValue()).intValue();
            }

            return (Integer) this.getValue();
        }

        public Double getDoubleValue() {
            if (this.type == Number.class) {
                return ((Number) this.getValue()).doubleValue();
            }

            return (Double) this.getValue();
        }

        public String getStringValue() {
            return (String) this.getValue();
        }
    }
}
