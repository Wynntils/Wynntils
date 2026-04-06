/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;

/**
 * Represents a part of an info variable template.
 * A template part can be either a (string) literal or en expression that will be evaluated.
 */
public abstract class TemplatePart {
    protected final String part;

    protected TemplatePart(String part) {
        this.part = part;
    }

    public abstract StyledText getValue();

    public String getCodedValue() {
        return getValue().getString(StyleType.INCLUDE_SPECIALS);
    }

    @Override
    public abstract String toString();
}
