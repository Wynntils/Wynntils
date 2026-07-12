/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@FunctionalInterface
public interface TooltipIdentificationDecorator {
    default MutableComponent getTitle(Component title) {
        return title.copy();
    }

    MutableComponent getSuffix(StatActualValue actualValue, StatPossibleValues possibleValues, TooltipStyle style);
}
