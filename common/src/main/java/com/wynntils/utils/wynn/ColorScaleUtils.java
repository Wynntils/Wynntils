/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.utils.MathUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class ColorScaleUtils {
    private static final NavigableMap<Float, TextColor> LERP_MAP = new TreeMap<>(Map.of(
            0f,
            TextColor.fromLegacyFormat(ChatFormatting.RED),
            70f,
            TextColor.fromLegacyFormat(ChatFormatting.YELLOW),
            90f,
            TextColor.fromLegacyFormat(ChatFormatting.GREEN),
            100f,
            TextColor.fromLegacyFormat(ChatFormatting.AQUA)));

    private static final NavigableMap<Float, TextColor> FLAT_MAP = new TreeMap<>(Map.of(
            30f,
            TextColor.fromLegacyFormat(ChatFormatting.RED),
            80f,
            TextColor.fromLegacyFormat(ChatFormatting.YELLOW),
            96f,
            TextColor.fromLegacyFormat(ChatFormatting.GREEN),
            Float.MAX_VALUE,
            TextColor.fromLegacyFormat(ChatFormatting.AQUA)));

    /**
     * Create the colored percentage component for an item ID
     *
     * @param percentage the percent roll of the ID
     * @return the styled percentage text component
     */
    public static MutableComponent getPercentageTextComponent(float percentage, boolean colorLerp, int decimalPlaces) {
        Style color = Style.EMPTY
                .withColor(colorLerp ? getPercentageColor(percentage) : getFlatPercentageColor(percentage))
                .withItalic(false);
        String percentString = new BigDecimal(percentage)
                .setScale(decimalPlaces, RoundingMode.DOWN)
                .toPlainString();
        return Component.literal(" [" + percentString + "%]").withStyle(color);
    }

    private static TextColor getPercentageColor(float percentage) {
        Map.Entry<Float, TextColor> lowerEntry = LERP_MAP.floorEntry(percentage);
        Map.Entry<Float, TextColor> higherEntry = LERP_MAP.ceilingEntry(percentage);

        // Boundary conditions
        if (lowerEntry == null) {
            return higherEntry.getValue();
        } else if (higherEntry == null) {
            return lowerEntry.getValue();
        }

        if (Objects.equals(lowerEntry.getKey(), higherEntry.getKey())) {
            return lowerEntry.getValue();
        }

        float t = MathUtils.inverseLerp(lowerEntry.getKey(), higherEntry.getKey(), percentage);

        int lowerColor = lowerEntry.getValue().getValue();
        int higherColor = higherEntry.getValue().getValue();

        int r = (int) MathUtils.lerp((lowerColor >> 16) & 0xff, (higherColor >> 16) & 0xff, t);
        int g = (int) MathUtils.lerp((lowerColor >> 8) & 0xff, (higherColor >> 8) & 0xff, t);
        int b = (int) MathUtils.lerp(lowerColor & 0xff, higherColor & 0xff, t);

        return TextColor.fromRgb((r << 16) | (g << 8) | b);
    }

    private static TextColor getFlatPercentageColor(float percentage) {
        return FLAT_MAP.higherEntry(percentage).getValue();
    }
}
