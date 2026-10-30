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
        RollStyle identifiedOverallRollStyle,
        RollStyle craftedOverallRollStyle,
        boolean perfectTitle,
        boolean defectiveTitle,
        boolean overallRollInSpecialName,
        ItemWeightSource itemWeightSource,
        WeightDisplay weightDisplay,
        StatListOrdering identificationOrdering,
        boolean groupIdentifications,
        IdentificationDisplay identificationDisplay,
        boolean showBestValueLastAlways,
        boolean rainbowInternalRoll,
        RollStyle identifiedRollStyle,
        RollStyle craftedRollStyle,
        int decimalPlaces,
        NavigableMap<Float, TextColor> colorMap,
        boolean colorLerp) {
    public static final TooltipOptions DEFAULT = new TooltipOptions(
            RollStyle.PERCENTAGE,
            RollStyle.NONE,
            false,
            false,
            false,
            ItemWeightSource.NONE,
            WeightDisplay.OVERALL,
            StatListOrdering.WYNNCRAFT,
            true,
            IdentificationDisplay.PERCENTAGE,
            true,
            true,
            RollStyle.WHEEL,
            RollStyle.WHEEL,
            0,
            Collections.emptyNavigableMap(),
            false);

    public enum IdentificationDisplay {
        PERCENTAGE,
        STAT_RANGES,
        REROLL_CHANCE,
        INTERNAL_ROLL
    }

    public enum WeightDisplay {
        OVERALL,
        DISTRIBUTION,
        CONTRIBUTION
    }

    public enum RollStyle {
        PERCENTAGE,
        WHEEL,
        NONE
    }
}
