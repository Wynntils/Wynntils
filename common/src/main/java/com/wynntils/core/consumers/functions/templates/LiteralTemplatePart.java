/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates;

public class LiteralTemplatePart extends TemplatePart {
    public LiteralTemplatePart(String part) {
        super(part);
    }

    @Override
    public String getValue() {
        return part;
    }

    @Override
    public String toString() {
        return "LiteralTemplatePart{" + "part='" + part + "'}";
    }
}
