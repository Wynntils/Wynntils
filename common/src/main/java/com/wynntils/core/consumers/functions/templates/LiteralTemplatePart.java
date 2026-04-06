/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates;

import com.wynntils.core.text.StyledText;

public class LiteralTemplatePart extends TemplatePart {
    public LiteralTemplatePart(String part) {
        super(part);
    }

    @Override
    public StyledText getValue() {
        return StyledText.fromString(part);
    }

    @Override
    public String getCodedValue() {
        return part;
    }

    @Override
    public String toString() {
        return "LiteralTemplatePart{" + "part='" + part + "'}";
    }
}
