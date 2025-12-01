/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates.parser;

import com.wynntils.core.consumers.functions.templates.ExpressionTemplatePart;
import com.wynntils.core.consumers.functions.templates.LiteralTemplatePart;
import com.wynntils.core.consumers.functions.templates.Template;
import com.wynntils.core.consumers.functions.templates.TemplatePart;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TemplateParser {
    public static Template getTemplateFromString(String templateString) {
        List<TemplatePart> parts = parseTemplate(templateString);

        return new Template(Collections.unmodifiableList(parts));
    }

    private static List<TemplatePart> parseTemplate(String templateString) {
        List<TemplatePart> parts = new ArrayList<>();

        final int lastIndexOfExpressionEnd = templateString.lastIndexOf('}');

        int expressionContextStart = -1;
        int expressionNestLevel = 0;
        int processedUntil = 0;

        for (int i = 0; i < templateString.length(); i++) {
            char current = templateString.charAt(i);

            if (current == '{') {
                // Handle if we are already in an expression
                if (expressionContextStart != -1) {
                    expressionNestLevel++;
                    continue;
                }

                if (processedUntil != i) {
                    parts.add(new LiteralTemplatePart(templateString.substring(processedUntil, i)));
                    processedUntil = i;
                }

                expressionContextStart = i;
                continue;
            }

            if (current == '}' && expressionContextStart != -1) {
                if (expressionNestLevel != 0) {
                    // Check if we have an asymmetric, but "complete" expression
                    // While this is likely a mistake, we will try to parse it anyway
                    // ("{{expression} {expression2}")

                    if (i != lastIndexOfExpressionEnd) {
                        expressionNestLevel--;
                        continue;
                    }
                }

                // We have a complete expression
                String expression = templateString.substring(expressionContextStart + 1, i);
                parts.add(new ExpressionTemplatePart(templateString.substring(expressionContextStart, i + 1)));

                // Reset the expression context
                expressionContextStart = -1;
                expressionNestLevel = 0;
                processedUntil = i + 1;
            }
        }

        if (processedUntil != templateString.length()) {
            // Add the remaining part
            parts.add(new LiteralTemplatePart(templateString.substring(processedUntil)));
        }

        return parts;
    }
}
