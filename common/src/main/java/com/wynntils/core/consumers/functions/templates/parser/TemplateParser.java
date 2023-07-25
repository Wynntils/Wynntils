/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates.parser;

import com.wynntils.core.consumers.functions.templates.ExpressionTemplatePart;
import com.wynntils.core.consumers.functions.templates.LiteralTemplatePart;
import com.wynntils.core.consumers.functions.templates.TemplatePart;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class TemplateParser {
    public static String doFormat(String templateString) {
        List<TemplatePart> parts = parseTemplate(templateString);

        return parts.stream().map(TemplatePart::getValue).collect(Collectors.joining());
    }

    private static List<TemplatePart> parseTemplate(String templateString) {
        List<TemplatePart> parts = new ArrayList<>();

        final int lastIndexOfExpresionEnd = templateString.lastIndexOf('}');

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
                    // Check if we have an asymmetric, but "complete" expresion
                    // While this is likely a mistake, we will try to parse it anyway
                    // ("{{expression} {expression2}")

                    if (i != lastIndexOfExpresionEnd) {
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
