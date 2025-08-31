/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record ItemObtainInfo(ItemObtainType sourceType, Optional<String> name) {
    public static final Codec<ItemObtainInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemObtainType.CODEC.fieldOf("sourceType").forGetter(ItemObtainInfo::sourceType),
                    Codec.STRING.optionalFieldOf("name").forGetter(ItemObtainInfo::name))
            .apply(instance, ItemObtainInfo::new));
}
