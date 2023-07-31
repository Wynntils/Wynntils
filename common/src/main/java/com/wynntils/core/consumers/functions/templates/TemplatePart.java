/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates;

/**
 * Represents a part of an info variable template.
 * A template part can be either a (string) literal or en expression that will be evaluated.
 */
public abstract class TemplatePart {
    protected final String part;

    protected TemplatePart(String part) {
        this.part = part;
    }

    public abstract String getValue();

    @Override
    public abstract String toString();
}
