/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.text.fonts.wynnfonts.TooltipIdentificationMeterFont;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.type.CappedValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import net.minecraft.ChatFormatting;
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
            NavigableMap<Float, TextColor> colorMap,
            float percentage,
            boolean colorLerp,
            boolean rainbow,
            int decimalPlaces) {
        return getPercentageTextComponent(colorMap, percentage, colorLerp, rainbow, decimalPlaces, false);
    }

    public static MutableComponent getPercentageTextComponent(
            NavigableMap<Float, TextColor> colorMap,
            float percentage,
            boolean colorLerp,
            boolean rainbow,
            int decimalPlaces,
            boolean estimated) {
        Style color = Style.EMPTY.withColor(
                rainbow
                        ? TextColor.fromRgb(WynncraftShaderColor.RAINBOW.color.asInt())
                        : colorLerp
                                ? getPercentageColor(colorMap, percentage)
                                : getFlatPercentageColor(colorMap, percentage));
        String percentString = new BigDecimal(percentage)
                .setScale(decimalPlaces, RoundingMode.DOWN)
                .toPlainString();
        return Component.literal(
                        " [" + (estimated && percentage != 0 && percentage != 100 ? "~" : "") + percentString + "%]")
                .withStyle(color);
    }

    public static MutableComponent getWheelTextComponent(
            NavigableMap<Float, TextColor> colorMap, float percentage, boolean colorLerp, boolean rainbow) {
        return getWheelTextComponent(
                colorMap, percentage, CappedValue.fromProgress(percentage / 100, 100), colorLerp, rainbow);
    }

    public static MutableComponent getWheelTextComponent(
            NavigableMap<Float, TextColor> colorMap, CappedValue meter, boolean colorLerp, boolean rainbow) {
        return getWheelTextComponent(colorMap, (float) meter.getPercentage(), meter, colorLerp, rainbow);
    }

    private static MutableComponent getWheelTextComponent(
            NavigableMap<Float, TextColor> colorMap,
            float percentage,
            CappedValue meter,
            boolean colorLerp,
            boolean rainbow) {
        CustomColor fillColor = rainbow
                ? WynncraftShaderColor.RAINBOW.color
                : CustomColor.fromTextColor(
                        colorLerp
                                ? getPercentageColor(colorMap, percentage)
                                : getFlatPercentageColor(colorMap, percentage));
        return TooltipIdentificationMeterFont.buildCounterSingleLayerMeter(
                        meter, fillColor, CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY), "")
                .copy();
    }

    public static TextColor getPotentialColor(NavigableMap<Integer, TextColor> colorMap, int potential) {
        Map.Entry<Integer, TextColor> entry = colorMap.floorEntry(potential);

        if (entry == null) {
            return colorMap.firstEntry().getValue();
        }

        return entry.getValue();
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
