/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.utils.mc.type.CodedString;
import com.wynntils.utils.render.FontRenderer;
import java.util.Arrays;
import net.minecraft.client.gui.Font;

public final class RenderedStringUtils {
    public static CodedString[] wrapTextBySize(CodedString s, int maxPixels) {
        Font font = McUtils.mc().font;
        int spaceSize = font.width(" ");

        String[] stringArray = s.str().split(" ");
        StringBuilder result = new StringBuilder();
        int length = 0;

        for (String string : stringArray) {
            String[] lines = string.split("\\\\n", -1);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (i > 0 || length + font.width(line) >= maxPixels) {
                    result.append('\n');
                    length = 0;
                }
                if (!line.isEmpty()) {
                    result.append(line).append(' ');
                    length += font.width(line) + spaceSize;
                }
            }
        }

        return Arrays.stream(result.toString().split("\n")).map(CodedString::of).toArray(CodedString[]::new);
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

    public static String trySplitOptimally(String line, float maxWidth) {
        String maxFitting = RenderedStringUtils.getMaxFittingText(
                line, maxWidth, FontRenderer.getInstance().getFont());

        if (maxFitting.contains("[") && !maxFitting.contains("]")) { // Detail line did not appear to fit, force break
            String color = "";

            if (line.startsWith("§")) {
                color = line.substring(0, 2);
            }

            return line.replaceFirst(" \\[", "\n" + color + "[");
        } else if (maxFitting.contains("(")
                && !maxFitting.contains(")")) { // Detail line did not appear to fit, force break
            String color = "";

            if (line.startsWith("§")) {
                color = line.substring(0, 2);
            }

            return line.replaceFirst(" \\(", "\n" + color + "(");
        } else { // Fits fine, give normal lines
            return line;
        }
    }
}
