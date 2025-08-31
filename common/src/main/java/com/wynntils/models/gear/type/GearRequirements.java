/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

public record GearRequirements(
        int level, Optional<ClassType> classType, List<Pair<Skill, Integer>> skills, Optional<String> quest) {
    public static final Codec<GearRequirements> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("level").forGetter(GearRequirements::level),
                    ClassType.CODEC.optionalFieldOf("classType").forGetter(GearRequirements::classType),
                    Pair.codec(Skill.CODEC, Codec.INT)
                            .listOf()
                            .fieldOf("skills")
                            .forGetter(GearRequirements::skills),
                    Codec.STRING.optionalFieldOf("quest").forGetter(GearRequirements::quest))
            .apply(instance, GearRequirements::new));
}
