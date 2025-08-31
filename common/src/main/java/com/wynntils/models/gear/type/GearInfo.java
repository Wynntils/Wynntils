/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wynntils.models.stats.type.FixedStats;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record GearInfo(
        String name,
        GearType type,
        GearTier tier,
        int powderSlots,
        GearMetaInfo metaInfo,
        GearRequirements requirements,
        FixedStats fixedStats,
        List<Pair<StatType, StatPossibleValues>> variableStats,
        Optional<SetInfo> setInfo) {
    public static final Codec<GearInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(GearInfo::name),
                    GearType.CODEC.fieldOf("type").forGetter(GearInfo::type),
                    GearTier.CODEC.fieldOf("tier").forGetter(GearInfo::tier),
                    Codec.INT.fieldOf("powderSlots").forGetter(GearInfo::powderSlots),
                    GearMetaInfo.CODEC.fieldOf("metaInfo").forGetter(GearInfo::metaInfo),
                    GearRequirements.CODEC.fieldOf("requirements").forGetter(GearInfo::requirements),
                    FixedStats.CODEC.fieldOf("fixedStats").forGetter(GearInfo::fixedStats),
                    Pair.codec(StatType.CODEC, StatPossibleValues.CODEC)
                            .listOf()
                            .fieldOf("variableStats")
                            .forGetter(GearInfo::variableStats),
                    SetInfo.CODEC.optionalFieldOf("setInfo").forGetter(GearInfo::setInfo))
            .apply(instance, GearInfo::new));

    public StatPossibleValues getPossibleValues(StatType statType) {
        return this.variableStats().stream()
                .filter(p -> p.key().equals(statType))
                .findFirst()
                .map(Pair::value)
                .orElse(null);
    }

    public List<StatType> getVariableStats() {
        return variableStats().stream().map(Pair::key).toList();
    }

    public Map<StatType, StatPossibleValues> getVariableStatsMap() {
        return variableStats().stream().collect(Collectors.toMap(Pair::key, Pair::value));
    }

    public List<StatPossibleValues> getPossibleValueList() {
        return variableStats().stream().map(Pair::value).toList();
    }
}
