/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates;

import com.wynntils.core.text.StyledText;
import java.util.List;
import java.util.stream.Collectors;

public class Template {
    private final List<TemplatePart> parts;

    public Template(List<TemplatePart> parts) {
        this.parts = parts;
    }

    public StyledText getStyledText() {
        String codedString = parts.stream().map(TemplatePart::getCodedValue).collect(Collectors.joining());

        return StyledText.fromString(codedString);
    }
}
