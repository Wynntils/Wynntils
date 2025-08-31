/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.mojang.serialization.Codec;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

public record FixedStats(
        int healthBuff,
        Optional<GearAttackSpeed> attackSpeed,
        Optional<GearMajorId> majorIds,
        List<Pair<DamageType, RangedValue>> damages,
        List<Pair<Element, Integer>> defences) {
    public static final Codec<FixedStats> CODEC =
            com.mojang.serialization.codecs.RecordCodecBuilder.create(instance -> instance.group(
                            Codec.INT.fieldOf("healthBuff").forGetter(FixedStats::healthBuff),
                            GearAttackSpeed.CODEC.optionalFieldOf("attackSpeed").forGetter(FixedStats::attackSpeed),
                            GearMajorId.CODEC.optionalFieldOf("majorIds").forGetter(FixedStats::majorIds),
                            Pair.codec(DamageType.CODEC, RangedValue.CODEC)
                                    .listOf()
                                    .fieldOf("damages")
                                    .forGetter(FixedStats::damages),
                            Pair.codec(Element.CODEC, Codec.INT)
                                    .listOf()
                                    .fieldOf("defences")
                                    .forGetter(FixedStats::defences))
                    .apply(instance, FixedStats::new));
}
