/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.render.FontRenderer;
import java.util.Arrays;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public final class RenderedStringUtils {
    private static final Pattern OPENING_PARENTHESIS_PATTERN = Pattern.compile(" \\(");
    private static final Pattern OPENING_BRACKET_PATTERN = Pattern.compile(" \\[");

    public static StyledText[] wrapTextBySize(StyledText s, int maxPixels) {
        Font font = McUtils.mc().font;
        int spaceSize = font.width(" ");

        StyledText[] stringArray = s.split(" ");
        StringBuilder result = new StringBuilder();
        int length = 0;

        for (StyledText string : stringArray) {
            StyledText[] lines = string.split("\\\\n");
            for (int i = 0; i < lines.length; i++) {
                StyledText line = lines[i];
                if (i > 0 || length + font.width(line.getString()) >= maxPixels) {
                    result.append('\n');
                    length = 0;
                }
                if (!line.isEmpty()) {
                    result.append(line.getString()).append(' ');
                    length += font.width(line.getString()) + spaceSize;
                }
            }
        }

        return Arrays.stream(result.toString().split("\n"))
                .map(StyledText::fromString)
                .toArray(StyledText[]::new);
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
                line.getStringWithoutFormatting(),
                maxWidth,
                FontRenderer.getInstance().getFont());

        if (maxFitting.contains("[") && !maxFitting.contains("]")) { // Detail line did not appear to fit, force break
            String color = "";

            if (line.startsWith("§")) {
                color = line.getString().substring(0, 2);
            }

            return StyledText.fromString(
                    OPENING_BRACKET_PATTERN.matcher(line.getString()).replaceFirst("\n" + color + "["));
        } else if (maxFitting.contains("(")
                && !maxFitting.contains(")")) { // Detail line did not appear to fit, force break
            String color = "";

            if (line.startsWith("§")) {
                color = line.getString().substring(0, 2);
            }

            return StyledText.fromString(
                    OPENING_PARENTHESIS_PATTERN.matcher(line.getString()).replaceFirst("\n" + color + "("));
        } else { // Fits fine, give normal lines
            return line;
        }
    }

    public static String substringMaxWidth(String input, int maxWidth) {
        Font font = McUtils.mc().font;
        if (font.width(input) <= maxWidth) return input;

        StringBuilder builder = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (font.width(builder.toString() + c) > maxWidth) break;
            builder.append(c);
        }

        return builder.toString();
    }

    public static Component getPercentageComponent(int count, int totalCount, int tickCount) {
        return getPercentageComponent(count, totalCount, tickCount, false, "");
    }

    public static Component getPercentageComponent(
            int count, int totalCount, int tickCount, boolean displayRawCount, String amountSuffix) {
        int percentage = Math.round((float) count / totalCount * 100);
        ChatFormatting foregroundColor;
        ChatFormatting braceColor;

        if (percentage < 25) {
            braceColor = ChatFormatting.DARK_RED;
            foregroundColor = ChatFormatting.RED;
        } else if (percentage < 75) {
            braceColor = ChatFormatting.GOLD;
            foregroundColor = ChatFormatting.YELLOW;
        } else {
            braceColor = ChatFormatting.DARK_GREEN;
            foregroundColor = ChatFormatting.GREEN;
        }

        StringBuilder insideText = new StringBuilder(foregroundColor.toString());
        if (displayRawCount) {
            insideText
                    .append("|".repeat(tickCount))
                    .append(count)
                    .append(amountSuffix)
                    .append("|".repeat(tickCount));
        } else {
            insideText
                    .append("|".repeat(tickCount))
                    .append(percentage)
                    .append("%")
                    .append("|".repeat(tickCount));
        }
        int insertAt =
                Math.min(insideText.length(), Math.round((insideText.length() - 2) * (float) count / totalCount) + 2);
        insideText.insert(insertAt, ChatFormatting.DARK_GRAY);

        return Component.literal("[")
                .withStyle(braceColor)
                .append(Component.literal(insideText.toString()))
                .append(Component.literal("]").withStyle(braceColor));
    }
}
