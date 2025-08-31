/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wynntils.utils.type.RangedValue;

// The range is actually possible derive from the other values, but is so commonly used
// that we cache it here as well
public record StatPossibleValues(StatType statType, RangedValue range, int baseValue, boolean isPreIdentified) {
    public static final Codec<StatPossibleValues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    StatType.CODEC.fieldOf("statType").forGetter(StatPossibleValues::statType),
                    RangedValue.CODEC.fieldOf("range").forGetter(StatPossibleValues::range),
                    Codec.INT.fieldOf("baseValue").forGetter(StatPossibleValues::baseValue),
                    Codec.BOOL.fieldOf("isPreIdentified").forGetter(StatPossibleValues::isPreIdentified))
            .apply(instance, StatPossibleValues::new));
}
