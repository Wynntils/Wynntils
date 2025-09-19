/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.arguments;

import com.wynntils.utils.type.ErrorOr;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class FunctionArguments {
    private final List<Argument<?>> arguments;
    private final Map<String, Argument<?>> lookupMap;

    private FunctionArguments(List<Argument<?>> arguments) {
        this.arguments = arguments;

        this.lookupMap = this.arguments.stream().collect(Collectors.toMap(Argument::getName, argument -> argument));
    }

    public Argument<?> getArgument(String name) {
        return this.lookupMap.get(name);
    }

    public abstract static class Builder {
        protected final List<Argument<?>> arguments;

        protected Builder(List<Argument<?>> arguments) {
            this.arguments = arguments;
        }

        public ErrorOr<FunctionArguments> buildWithValues(List<Object> values) {
            if (arguments.stream()
                            .filter(argument -> argument instanceof ListArgument<?>)
                            .count()
                    > 1) {
                throw new IllegalArgumentException("Only one list argument is allowed.");
            }

            boolean hasListArgument = arguments.stream().anyMatch(argument -> argument instanceof ListArgument<?>);
            if (hasListArgument && !(arguments.getLast() instanceof ListArgument<?>)) {
                throw new IllegalArgumentException("List argument needs to be the last argument.");
            }

            if (!hasListArgument && values.size() != this.arguments.size()) {
                return ErrorOr.error("Invalid number of arguments");
            }

            for (int i = 0; i < this.arguments.size(); i++) {
                Argument<?> argument = this.arguments.get(i);

                if (argument instanceof ListArgument<?> listArgument) {
                    List<Object> listValues = values.subList(i, values.size());

                    Optional<Object> nonMatchingValue = listValues.stream()
                            .filter(value -> !listArgument.getInnerType().isAssignableFrom(value.getClass()))
                            .findFirst();
                    if (nonMatchingValue.isPresent()) {
                        return ErrorOr.error("Invalid argument type in list argument: \"%s\" is not a %s"
                                .formatted(
                                        nonMatchingValue.get(),
                                        argument.getType().getSimpleName()));
                    }

                    listArgument.setValue(listValues);
                    break;
                }

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

        public List<Argument<?>> getArguments() {
            return Collections.unmodifiableList(arguments);
        }

        public int getArgumentCount() {
            return arguments.size();
        }
    }

    // A builder for functions with required arguments
    public static class RequiredArgumentBuilder extends Builder {
        public static final Builder EMPTY = new RequiredArgumentBuilder(List.of());

        public RequiredArgumentBuilder(List<Argument<?>> arguments) {
            super(arguments);
        }
    }

    // A builder for functions with optional arguments
    public static class OptionalArgumentBuilder extends Builder {
        public static final Builder EMPTY = new OptionalArgumentBuilder(List.of());

        public OptionalArgumentBuilder(List<Argument<?>> arguments) {
            super(arguments);

            if (arguments.stream().anyMatch(argument -> argument instanceof ListArgument<?>)) {
                throw new IllegalArgumentException("List arguments are not supported for optional arguments.");
            }
        }

        public List<Object> getDefaults() {
            return this.arguments.stream().map(Argument::getDefaultValue).collect(Collectors.toList());
        }
    }
}
