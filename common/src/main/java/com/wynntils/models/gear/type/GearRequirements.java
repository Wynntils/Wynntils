/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

public record GearRequirements(
        int level,
        Optional<ClassType> classType,
        List<Pair<Skill, Integer>> skills,
        Optional<String> quest,
        int questLevel) {
    public GearRequirements(
            int level, Optional<ClassType> classType, List<Pair<Skill, Integer>> skills, Optional<String> quest) {
        this(level, classType, skills, quest, 0);
    }

    public GearRequirements withParsedQuestRequirement(GearRequirements parsed) {
        // Retain the full API name only when it matches the quest displayed on the item.
        Optional<String> questName = parsed.quest()
                .map(name -> quest.filter(
                                apiName -> StringUtils.shorten(apiName, 10).equalsIgnoreCase(name))
                        .orElse(name));
        return new GearRequirements(level, classType, skills, questName, parsed.questLevel());
    }
}
