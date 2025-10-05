/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.arguments;

import java.util.List;

public class ListArgument<T> extends Argument<List> {
    private final Class<T> innerType;

    public ListArgument(String name, Class<T> innerType) {
        super(name, List.class, null, false);

        if (!SUPPORTED_ARGUMENT_TYPES.contains(innerType)) {
            throw new IllegalArgumentException("Unsupported inner argument type: " + innerType);
        }

        this.innerType = innerType;
    }

    public Class<T> getInnerType() {
        return innerType;
    }

    @SuppressWarnings("unchecked")
    public <U> List<U> getList(Class<U> assumedType) {
        if (!assumedType.equals(this.innerType)) {
            throw new IllegalStateException("List argument is not a " + assumedType.getSimpleName() + ".");
        }

        // Due to type erasure, we cannot check the type parameter of ListArgument
        // We can just cast it and hope for the best
        return (List<U>) getValueChecked(List.class);
    }
}
