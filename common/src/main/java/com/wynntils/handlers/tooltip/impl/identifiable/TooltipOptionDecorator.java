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
import com.wynntils.models.stats.StatCalculator.PercentageCalculation;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

public final class TooltipOptionDecorator implements TooltipIdentificationDecorator {
    private final IdentifiableItemProperty<?, ?> item;
    private final TooltipOptions options;
    private boolean hasEstimatedPercentages;

    public TooltipOptionDecorator(IdentifiableItemProperty<?, ?> item, TooltipOptions options) {
        this.item = item;
        this.options = options;
    }

    public boolean hasEstimatedPercentages() {
        return hasEstimatedPercentages;
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
            StatActualValue actualValue, @Nullable StatPossibleValues possibleValues, TooltipStyle style) {
        if (!options.identificationDecorations() || possibleValues == null) return Component.empty();

        IdentificationDisplay display = options.identificationDisplay();
        MutableComponent suffix =
                switch (display) {
                    case PERCENTAGE ->
                        buildPercentage(
                                actualValue, StatCalculator.calculatePercentage(actualValue, possibleValues), style);
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

        return suffix.withStyle(componentStyle -> componentStyle.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT));
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
                .withStyle(Style.EMPTY.withFont(FontDescription.DEFAULT).withColor(color));
    }

    private MutableComponent buildPercentage(
            StatActualValue actualValue, PercentageCalculation calculation, TooltipStyle style) {
        hasEstimatedPercentages |= calculation.estimated();
        MutableComponent percentage = ColorScaleUtils.getPercentageTextComponent(
                        options.colorMap(),
                        calculation.value(),
                        options.colorLerp(),
                        options.decimalPlaces(),
                        calculation.estimated())
                .withStyle(componentStyle -> componentStyle.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT));
        if (style.rainbowInternalRoll() && actualValue.stars() == 3) {
            percentage.withColor(WynncraftShaderColor.RAINBOW.color.asInt());
        }
        return percentage;
    }
}
