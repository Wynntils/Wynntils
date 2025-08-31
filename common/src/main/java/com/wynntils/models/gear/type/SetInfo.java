/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wynntils.models.stats.type.StatType;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;

public record SetInfo(String name, List<Map<StatType, Integer>> bonuses, List<String> items) {
    public static final Codec<SetInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(SetInfo::name),
                    Codec.unboundedMap(StatType.CODEC, Codec.INT)
                            .listOf()
                            .fieldOf("bonuses")
                            .forGetter(SetInfo::bonuses),
                    Codec.STRING.listOf().fieldOf("items").forGetter(SetInfo::items))
            .apply(instance, SetInfo::new));

    /**
     * @param numberOfItems The number of items equipped to get the set bonus for
     *                      (clamped to the number of items in the set)
     * @return A map of stat names to the bonus value for that stat
     */
    public Map<StatType, Integer> getBonusForItems(int numberOfItems) {
        return bonuses.get(Mth.clamp(numberOfItems, 1, items.size()) - 1);
    }
}
