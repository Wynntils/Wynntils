/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates;

import com.wynntils.utils.performance.Profiler;

import java.util.List;
import java.util.stream.Collectors;

public class Template {
    private final List<TemplatePart> parts;

    public Template(List<TemplatePart> parts) {
        this.parts = parts;
    }

    public String getString() {
        try (Profiler.Scope ignored = Profiler.scope("Template::getString")) {
            return parts.stream().map(TemplatePart::getValue).collect(Collectors.joining());
        }
    }

    public List<TemplatePart> getParts() {
        return parts;
    }
}
