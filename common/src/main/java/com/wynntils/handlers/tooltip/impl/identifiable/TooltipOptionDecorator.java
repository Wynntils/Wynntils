/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.wynn.ColorScaleUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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

        MutableComponent percentage = ColorScaleUtils.getPercentageTextComponent(
                        options.colorMap(),
                        StatCalculator.getPercentage(actualValue, possibleValues),
                        options.colorLerp(),
                        options.decimalPlaces())
                .withStyle(componentStyle -> componentStyle.withFont(CommonFonts.LANGUAGE_FONT));
        if (style.rainbowInternalRoll() && actualValue.stars() == 3) {
            percentage.withColor(WynncraftShaderColor.RAINBOW.color.asInt());
        }
        return percentage;
    }
}
