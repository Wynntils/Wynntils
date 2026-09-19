/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

final class CraftedTooltipOptionDecorator implements TooltipIdentificationDecorator {
    private final TooltipOptions options;
    private final Optional<Float> overallPercentage;

    CraftedTooltipOptionDecorator(CraftedItemProperty item, TooltipOptions options) {
        this.options = options;
        this.overallPercentage = StatCalculator.calculateOverallQualityFromVanillaMeters(item.getIdentifications());
    }

    @Override
    public MutableComponent getTitle(Component title) {
        MutableComponent decorated = title.copy();
        if (options.style().craftedPercentages()
                && options.overallPercentageInName()
                && overallPercentage.isPresent()) {
            decorated.append(ColorScaleUtils.getPercentageTextComponent(
                    options.colorMap(), overallPercentage.get(), options.colorLerp(), options.decimalPlaces(), true));
        }
        return decorated;
    }

    @Override
    public MutableComponent getSuffix(
            StatActualValue actualValue, StatPossibleValues possibleValues, TooltipStyle style) {
        if (!options.identificationDecorations()) return Component.empty();

        float percentage;
        if (actualValue.vanillaMeter().isPresent()) {
            percentage = StatCalculator.getPercentageFromVanillaMeter(
                    actualValue.vanillaMeter().get());
        } else {
            percentage = 0;
        }
        MutableComponent suffix = Component.empty();
        boolean rainbowPerfect = style.rainbowInternalRoll() && actualValue.perfectInternalRoll();

        if (style.craftedPercentages()) {
            suffix = ColorScaleUtils.getPercentageTextComponent(
                            options.colorMap(), percentage, options.colorLerp(), options.decimalPlaces(), true)
                    .withStyle(componentStyle -> componentStyle.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT));

            if (rainbowPerfect) {
                suffix.withColor(WynncraftShaderColor.RAINBOW.color.asInt());
            }
        } else if (options.style().showRollWheel()) {
            suffix = Component.literal(" ")
                    .append(ColorScaleUtils.getWheelTextComponent(
                            options.colorMap(), percentage, options.colorLerp(), rainbowPerfect));
        }
        return suffix;
    }
}
