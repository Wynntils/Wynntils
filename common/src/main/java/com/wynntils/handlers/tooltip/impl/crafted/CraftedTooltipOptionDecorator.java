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
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

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
        if (options.overallPercentageInName() && overallPercentage.isPresent()) {
            decorated.append(ColorScaleUtils.getPercentageTextComponent(
                    options.colorMap(), overallPercentage.get(), options.colorLerp(), options.decimalPlaces(), true));
        }
        return decorated;
    }

    @Override
    public MutableComponent getSuffix(
            StatActualValue actualValue, @Nullable StatPossibleValues possibleValues, TooltipStyle style) {
        if (!options.identificationDecorations() || actualValue.vanillaMeter().isEmpty()) {
            return Component.empty();
        }

        float percentage = StatCalculator.getPercentageFromVanillaMeter(
                actualValue.vanillaMeter().get());
        return ColorScaleUtils.getPercentageTextComponent(
                        options.colorMap(), percentage, options.colorLerp(), options.decimalPlaces(), true)
                .withStyle(componentStyle -> componentStyle.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT));
    }
}
