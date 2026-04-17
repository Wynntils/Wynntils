/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.arguments;

/**
 * An argument that is of an unspecified type
 */
public class AnyArgument extends Argument<Object> {
    public AnyArgument(String name) {
        super(name, Object.class, null, false);
    }
}
