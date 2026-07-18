/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.stats.type.StatListOrdering;
import java.util.Collections;
import java.util.NavigableMap;
import net.minecraft.network.chat.TextColor;

public record TooltipOptions(
        TooltipStyle style,
        boolean perfectTitle,
        boolean defectiveTitle,
        boolean identificationDecorations,
        ItemWeightSource itemWeightSource,
        boolean overallPercentageInName,
        boolean overallPercentageInSpecialName,
        NavigableMap<Float, TextColor> colorMap,
        boolean colorLerp,
        int decimalPlaces) {
    public static final TooltipOptions DEFAULT = new TooltipOptions(
            new TooltipStyle(StatListOrdering.WYNNCRAFT, false, false, true, true),
            false,
            false,
            false,
            ItemWeightSource.NONE,
            false,
            false,
            Collections.emptyNavigableMap(),
            false,
            0);
}
