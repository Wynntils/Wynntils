/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wynntils.core.text.StyledText;

public record GearMajorId(String name, StyledText lore) {
    public static final Codec<GearMajorId> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(GearMajorId::name),
                    StyledText.CODEC.fieldOf("lore").forGetter(GearMajorId::lore))
            .apply(instance, GearMajorId::new));
}
