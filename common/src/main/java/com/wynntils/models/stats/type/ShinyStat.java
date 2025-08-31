/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ShinyStat(ShinyStatType statType, long value, int shinyRerolls) {
    public static final Codec<ShinyStat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ShinyStatType.CODEC.fieldOf("statType").forGetter(ShinyStat::statType),
                    Codec.LONG.fieldOf("value").forGetter(ShinyStat::value),
                    Codec.INT.fieldOf("shinyRerolls").forGetter(ShinyStat::shinyRerolls))
            .apply(instance, ShinyStat::new));
}
