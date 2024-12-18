/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.utils.MathUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class ColorScaleUtils {
    /**
     * Create the colored percentage component for an item ID
     *
     * @param colorMap the colorMap that should be followed
     * @param percentage the percent roll of the ID
     * @param colorLerp should the colors lerp
     * @param decimalPlaces how many decimal places should be shown
     *
     * @return the styled percentage text component
     */
    public static MutableComponent getPercentageTextComponent(
            NavigableMap<Float, TextColor> colorMap, float percentage, boolean colorLerp, int decimalPlaces) {
        Style color = Style.EMPTY
                .withColor(
                        colorLerp
                                ? getPercentageColor(colorMap, percentage)
                                : getFlatPercentageColor(colorMap, percentage))
                .withItalic(false);
        String percentString = new BigDecimal(percentage)
                .setScale(decimalPlaces, RoundingMode.DOWN)
                .toPlainString();
        return Component.literal(" [" + percentString + "%]").withStyle(color);
    }

    private static TextColor getPercentageColor(NavigableMap<Float, TextColor> colorMap, float percentage) {
        Map.Entry<Float, TextColor> lowerEntry = colorMap.floorEntry(percentage);
        Map.Entry<Float, TextColor> higherEntry = colorMap.ceilingEntry(percentage);

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

    private static TextColor getFlatPercentageColor(NavigableMap<Float, TextColor> colorMap, float percentage) {
        return colorMap.higherEntry(percentage).getValue();
    }
}
