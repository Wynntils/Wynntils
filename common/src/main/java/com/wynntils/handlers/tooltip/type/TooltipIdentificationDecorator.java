/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import net.minecraft.network.chat.MutableComponent;

@FunctionalInterface
public interface TooltipIdentificationDecorator {
    MutableComponent getSuffix(StatActualValue statActualValue, StatPossibleValues possibleValues, TooltipStyle style);
}
