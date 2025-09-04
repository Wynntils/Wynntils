/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wynntils.core.components.Models;
import com.wynntils.models.stats.type.StatType;
import java.util.Map;

/**
 * Represents an "instance" of a unique set worn.
 * <p>
 * wynnCount may be inaccurate when the user has two of the same ring equipped (wynncraft bug).
 * Use SetModel to determine the true count if necessary.
 */
public record SetInstance(
        SetInfo setInfo, Map<String, Boolean> activeItems, int wynnCount, Map<StatType, Integer> wynnBonuses) {
    public static final Codec<SetInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    SetInfo.CODEC.fieldOf("setInfo").forGetter(SetInstance::setInfo),
                    Codec.unboundedMap(Codec.STRING, Codec.BOOL)
                            .fieldOf("activeItems")
                            .forGetter(SetInstance::activeItems),
                    Codec.INT.fieldOf("wynnCount").forGetter(SetInstance::wynnCount),
                    Codec.unboundedMap(Models.Stat.CODEC, Codec.INT)
                            .fieldOf("wynnBonuses")
                            .forGetter(SetInstance::wynnBonuses))
            .apply(instance, SetInstance::new));
}
