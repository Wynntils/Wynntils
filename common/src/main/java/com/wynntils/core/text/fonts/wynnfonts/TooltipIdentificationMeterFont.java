/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts.wynnfonts;

import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.RegisteredFont;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.CappedValue;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class TooltipIdentificationMeterFont extends RegisteredFont {
    public static final Integer STAGES = 36;
    private static final Component NEGATIVE_SPACE =
            Component.literal("\uDAFF\uDFF7").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT));
    private static final String EMPTY_METER_SPACE = "\uDB00\uDC09";
    public static final FontDescription TOOLTIP_IDENTIFICATION_METER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/identification/meter"));

    public TooltipIdentificationMeterFont() {
        super("wynncraft_tooltip_identification_meter");
    }

    public static Component buildCounterSingleLayerMeter(
            CappedValue value, CustomColor fillColor, CustomColor backgroundColor, String suffix) {
        MutableComponent component = Component.empty()
                .withStyle(Style.EMPTY.withFont(TOOLTIP_IDENTIFICATION_METER_FONT))
                .withoutShadow();

        component.append(Component.literal(WynnFont.toGlyph(STAGES + "_counter", TooltipIdentificationMeterFont.class))
                .withColor(backgroundColor.asInt()));

        if (value.current() != 0) {
            component.append(NEGATIVE_SPACE);

            int stage = normalizeToStage(value);

            if (stage == 0) {
                component.append(EMPTY_METER_SPACE);
            } else {
                component.append(
                        Component.literal(WynnFont.toGlyph(stage + "_counter", TooltipIdentificationMeterFont.class))
                                .withColor(fillColor.asInt()));
            }
        }

        component.append(suffix);

        return component;
    }

    public static Component buildCounterDoubleLayerMeter(
            CappedValue primaryValue,
            CappedValue secondaryValue,
            CustomColor primaryColor,
            CustomColor secondaryColor,
            CustomColor backgroundColor,
            String suffix) {
        MutableComponent component = Component.empty()
                .withStyle(Style.EMPTY.withFont(TOOLTIP_IDENTIFICATION_METER_FONT))
                .withoutShadow();

        if (primaryValue.max() <= 0 || secondaryValue.max() <= 0) {
            return component
                    .append(Component.literal(
                                    WynnFont.toGlyph(STAGES + "_counter", TooltipIdentificationMeterFont.class))
                            .withColor(backgroundColor.asInt()))
                    .append(suffix);
        }

        int primaryStage = Math.clamp(normalizeToStage(primaryValue), 0, STAGES);
        int secondaryStage = Math.clamp(normalizeToStage(secondaryValue), 0, STAGES);

        primaryStage = Math.min(primaryStage, secondaryStage);

        component.append(Component.literal(WynnFont.toGlyph(STAGES + "_counter", TooltipIdentificationMeterFont.class))
                .withColor(backgroundColor.asInt()));

        component.append(NEGATIVE_SPACE);
        if (secondaryStage > 0) {
            component.append(Component.literal(
                            WynnFont.toGlyph(secondaryStage + "_counter", TooltipIdentificationMeterFont.class))
                    .withColor(secondaryColor.asInt()));
        }

        component.append(NEGATIVE_SPACE);
        if (primaryStage == 0) {
            component.append(Component.literal(EMPTY_METER_SPACE));
        } else {
            component.append(
                    Component.literal(WynnFont.toGlyph(primaryStage + "_counter", TooltipIdentificationMeterFont.class))
                            .withColor(primaryColor.asInt()));
        }

        component.append(suffix);

        return component;
    }

    public static int meterStage(char c) {
        String clockwiseFirst = WynnFont.toGlyph("1", TooltipIdentificationMeterFont.class);
        String clockwiseLast = WynnFont.toGlyph(String.valueOf(STAGES), TooltipIdentificationMeterFont.class);

        String counterClockwiseFirst = WynnFont.toGlyph("1_counter", TooltipIdentificationMeterFont.class);
        String counterClockwiseLast = WynnFont.toGlyph(STAGES + "_counter", TooltipIdentificationMeterFont.class);

        if (c >= clockwiseFirst.charAt(0) && c <= clockwiseLast.charAt(0)) {
            return c - clockwiseFirst.charAt(0) + 1;
        }

        if (c >= counterClockwiseFirst.charAt(0) && c <= counterClockwiseLast.charAt(0)) {
            return c - counterClockwiseFirst.charAt(0) + 1;
        }

        return -1;
    }

    private static int normalizeToStage(CappedValue value) {
        if (value.max() <= 0) return 0;

        return (int) ((float) value.current() * STAGES / value.max());
    }
}
