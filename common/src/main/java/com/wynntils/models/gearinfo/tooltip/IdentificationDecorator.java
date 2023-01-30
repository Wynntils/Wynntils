package com.wynntils.models.gearinfo.tooltip;

import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import net.minecraft.network.chat.MutableComponent;

@FunctionalInterface
public interface IdentificationDecorator {
    MutableComponent getSuffix(StatActualValue statActualValue, StatPossibleValues possibleValues, GearTooltipStyle style);
}
