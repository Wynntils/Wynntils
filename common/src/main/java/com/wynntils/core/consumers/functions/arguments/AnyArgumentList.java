/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.arguments;

/**
 * An argument list that is of an unspecified type
 */
public class AnyArgumentList extends ListArgument<Object> {
    public AnyArgumentList(String name) {
        super(name, Object.class, false);
    }
}
