/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import java.util.List;
import java.util.Optional;

// The api name is normally the same as the name, but if not, the api name is given
// by apiName
public record GearMetaInfo(
        GearRestrictions restrictions,
        ItemMaterial material,
        List<ItemObtainInfo> obtainInfo,
        Optional<StyledText> lore,
        Optional<String> apiName,
        boolean allowCraftsman,
        boolean preIdentified) {
    public static final Codec<GearMetaInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    GearRestrictions.CODEC.fieldOf("restrictions").forGetter(GearMetaInfo::restrictions),
                    ItemMaterial.CODEC.fieldOf("material").forGetter(GearMetaInfo::material),
                    ItemObtainInfo.CODEC.listOf().fieldOf("obtainInfo").forGetter(GearMetaInfo::obtainInfo),
                    StyledText.CODEC.optionalFieldOf("lore").forGetter(GearMetaInfo::lore),
                    Codec.STRING.optionalFieldOf("apiName").forGetter(GearMetaInfo::apiName),
                    Codec.BOOL.fieldOf("allowCraftsman").forGetter(GearMetaInfo::allowCraftsman),
                    Codec.BOOL.fieldOf("preIdentified").forGetter(GearMetaInfo::preIdentified))
            .apply(instance, GearMetaInfo::new));
}
