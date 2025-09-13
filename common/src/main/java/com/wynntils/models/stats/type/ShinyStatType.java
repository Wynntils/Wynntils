/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wynntils.core.components.Models;

public record ShinyStatType(int id, String key, String displayName, StatUnit statUnit) {
    private static final Codec<ShinyStatType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("id").forGetter(ShinyStatType::id),
                    Codec.STRING.fieldOf("key").forGetter(ShinyStatType::key),
                    Codec.STRING.fieldOf("displayName").forGetter(ShinyStatType::displayName),
                    StatUnit.CODEC.fieldOf("statUnit").forGetter(ShinyStatType::statUnit))
            .apply(instance, ShinyStatType::new));
    private static final Codec<ShinyStatType> MODEL_LOOKUP_CODEC =
            Codec.INT.xmap(Models.Shiny::getShinyStatType, ShinyStatType::id);

    // Use withAlternative so that we save both only the ID for dynamic lookup using models
    // but also support direct serialization of the full object for cases where the model system is not available
    public static final Codec<ShinyStatType> CODEC = Codec.withAlternative(MODEL_LOOKUP_CODEC, DIRECT_CODEC);

    public static final ShinyStatType UNKNOWN = new ShinyStatType(0, "unknown", "Unknown", StatUnit.RAW);
}
