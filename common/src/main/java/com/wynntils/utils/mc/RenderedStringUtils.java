/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.render.FontRenderer;
import net.minecraft.client.gui.Font;

public final class RenderedStringUtils {
    public static StyledText[] wrapTextBySize(StyledText s, int maxPixels) {
        Font font = McUtils.mc().font;
        int spaceSize = font.width(" ");

        StyledText[] stringArray = s.split(" ");
        StyledText result = StyledText.EMPTY;
        int length = 0;

        // FIXME: codes should not count toward the word length

        for (StyledText string : stringArray) {
            StyledText[] lines = string.split("\\\\n");
            for (int i = 0; i < lines.length; i++) {
                StyledText line = lines[i];
                if (i > 0 || length + font.width(line.getInternalCodedStringRepresentation()) >= maxPixels) {
                    result = result.append("\n");
                    length = 0;
                }
                if (!line.isEmpty()) {
                    result = result.append(line).append(" ");
                    length += font.width(line.getInternalCodedStringRepresentation()) + spaceSize;
                }
            }
        }

        return result.split("\n");
    }

    public static String getMaxFittingText(String text, float maxTextWidth, Font font) {
        String renderedText;
        if (font.width(text) < maxTextWidth) {
            return text;
        } else {
            // This case, the input is too long, only render text that fits, and is closest to cursor
            StringBuilder builder = new StringBuilder();

            int suffixWidth = font.width("...");
            int stringPosition = 0;

            while (font.width(builder.toString()) < maxTextWidth - suffixWidth && stringPosition < text.length()) {
                builder.append(text.charAt(stringPosition));

                stringPosition++;
            }

            builder.append("...");
            renderedText = builder.toString();
        }
        return renderedText;
    }

    public static StyledText trySplitOptimally(StyledText line, float maxWidth) {
        String maxFitting = RenderedStringUtils.getMaxFittingText(
                line.withoutFormatting(), maxWidth, FontRenderer.getInstance().getFont());

        if (maxFitting.contains("[") && !maxFitting.contains("]")) { // Detail line did not appear to fit, force break
            String color = "";

            if (line.startsWith("§")) {
                color = line.getInternalCodedStringRepresentation().substring(0, 2);
            }

            return StyledText.fromString(
                    line.getInternalCodedStringRepresentation().replaceFirst(" \\[", "\n" + color + "["));
        } else if (maxFitting.contains("(")
                && !maxFitting.contains(")")) { // Detail line did not appear to fit, force break
            String color = "";

            if (line.startsWith("§")) {
                color = line.getInternalCodedStringRepresentation().substring(0, 2);
            }

            return StyledText.fromString(
                    line.getInternalCodedStringRepresentation().replaceFirst(" \\(", "\n" + color + "("));
        } else { // Fits fine, give normal lines
            return line;
        }
    }
}
