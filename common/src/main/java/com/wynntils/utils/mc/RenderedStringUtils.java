/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import net.minecraft.client.gui.Font;

public class RenderedStringUtils {
    public static String[] wrapTextBySize(String s, int maxPixels) {
        Font font = McUtils.mc().font;
        int spaceSize = font.width(" ");

        String[] stringArray = s.split(" ");
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

        return result.toString().split("\n");
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
}
