/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wynntils.core.components.Models;
import com.wynntils.utils.type.RangedValue;

public record StatActualValue(StatType statType, int value, int stars, RangedValue internalRoll) {
    public static final Codec<StatActualValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Models.Stat.CODEC.fieldOf("statType").forGetter(StatActualValue::statType),
                    Codec.INT.fieldOf("value").forGetter(StatActualValue::value),
                    Codec.INT.fieldOf("stars").forGetter(StatActualValue::stars),
                    RangedValue.CODEC.fieldOf("internalRoll").forGetter(StatActualValue::internalRoll))
            .apply(instance, StatActualValue::new));
}
