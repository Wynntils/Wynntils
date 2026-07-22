/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipOptions.IdentificationDisplay;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class TooltipOptionDecorator implements TooltipIdentificationDecorator {
    private final IdentifiableItemProperty<?, ?> item;
    private final TooltipOptions options;

    public TooltipOptionDecorator(IdentifiableItemProperty<?, ?> item, TooltipOptions options) {
        this.item = item;
        this.options = options;
    }

    @Override
    public MutableComponent getTitle(Component title) {
        boolean perfect = options.perfectTitle() && item.isPerfect();
        boolean defective = options.defectiveTitle() && item.isDefective();
        MutableComponent decorated;
        if (perfect) {
            decorated = ComponentUtils.makeRainbowStyle("Perfect " + title.getString(), true);
        } else if (defective) {
            decorated = ComponentUtils.makeCrimsonStyle("Defective " + title.getString(), true);
        } else {
            decorated = title.copy();
        }

        boolean showPercentage = item.hasOverallValue()
                && (perfect || defective
                        ? options.overallPercentageInSpecialName()
                        : options.overallPercentageInName());
        if (showPercentage) {
            decorated.append(ColorScaleUtils.getPercentageTextComponent(
                    options.colorMap(), item.getOverallPercentage(), options.colorLerp(), options.decimalPlaces()));
        }
        return decorated;
    }

    @Override
    public MutableComponent getSuffix(
            StatActualValue actualValue, StatPossibleValues possibleValues, TooltipStyle style) {
        if (!options.identificationDecorations()) return Component.empty();

        if (!possibleValues.range().inRange(actualValue.value())) {
            return Component.literal(" [NEW]").withStyle(ChatFormatting.GOLD);
        }

        IdentificationDisplay display = options.identificationDisplay();
        MutableComponent suffix =
                switch (display) {
                    case PERCENTAGE -> buildPercentage(actualValue, possibleValues, style);
                    case RANGE -> {
                        var range = StatCalculator.getDisplayRange(possibleValues, style.showBestValueLastAlways());
                        yield Component.literal(" [")
                                .append(Component.literal(range.a() + ", " + range.b())
                                        .withColor(CommonColors.WYNNCRAFT_GREEN.asInt()))
                                .append("]")
                                .withColor(CommonColors.WYNNCRAFT_DARK_GREEN.asInt());
                    }
                    case REROLL -> buildReroll(actualValue, possibleValues);
                    case INTERNAL_ROLL ->
                        Component.literal(" <")
                                .append(Component.literal(actualValue
                                                        .internalRoll()
                                                        .low() + "% to "
                                                + actualValue.internalRoll().high() + "%")
                                        .withColor(CommonColors.WYNNCRAFT_GREEN.asInt()))
                                .append(">")
                                .withColor(CommonColors.WYNNCRAFT_DARK_GREEN.asInt());
                };

        return suffix.withStyle(componentStyle -> componentStyle.withFont(CommonFonts.LANGUAGE_FONT));
    }

    private MutableComponent buildReroll(StatActualValue actualValue, StatPossibleValues possibleValues) {
        return Component.empty()
                .append(Component.literal(" "))
                .append(defaultGlyph("\u2605", CommonColors.WYNNCRAFT_AQUA.asInt()))
                .append(Component.literal(
                                String.format(Locale.ROOT, "%.2f%%", StatCalculator.getPerfectChance(possibleValues)))
                        .withColor(CommonColors.WYNNCRAFT_AQUA.asInt()))
                .append(Component.literal(" "))
                .append(defaultGlyph("\u21E7", CommonColors.WYNNCRAFT_GREEN.asInt()))
                .append(Component.literal(String.format(
                                Locale.ROOT, "%.1f%%", StatCalculator.getIncreaseChance(actualValue, possibleValues)))
                        .withColor(CommonColors.WYNNCRAFT_GREEN.asInt()))
                .append(Component.literal(" "))
                .append(defaultGlyph("\u21E9", CommonColors.WYNNCRAFT_RED.asInt()))
                .append(Component.literal(String.format(
                                Locale.ROOT, "%.1f%%", StatCalculator.getDecreaseChance(actualValue, possibleValues)))
                        .withColor(CommonColors.WYNNCRAFT_RED.asInt()));
    }

    private MutableComponent defaultGlyph(String glyph, int color) {
        return Component.literal(glyph)
                .withStyle(Style.EMPTY.withFont(CommonFonts.DEFAULT_FONT).withColor(color));
    }

    private MutableComponent buildPercentage(
            StatActualValue actualValue, StatPossibleValues possibleValues, TooltipStyle style) {
        MutableComponent percentage = ColorScaleUtils.getPercentageTextComponent(
                options.colorMap(),
                StatCalculator.getPercentage(actualValue, possibleValues),
                options.colorLerp(),
                options.decimalPlaces());
        if (style.rainbowInternalRoll() && actualValue.stars() == 3) {
            percentage.withColor(WynncraftShaderColor.RAINBOW.color.asInt());
        }
        return percentage;
    }
}
