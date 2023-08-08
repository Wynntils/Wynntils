/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates;

import java.util.List;
import java.util.stream.Collectors;

public class Template {
    private final List<TemplatePart> parts;

    public Template(List<TemplatePart> parts) {
        this.parts = parts;
    }

    public String getString() {
        return parts.stream().map(TemplatePart::getValue).collect(Collectors.joining());
    }
}
