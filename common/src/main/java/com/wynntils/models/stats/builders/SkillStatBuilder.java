/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatUnit;
import java.util.Locale;
import java.util.function.Consumer;

public final class SkillStatBuilder extends StatBuilder<SkillStatType> {
    @Override
    public void buildStats(Consumer<SkillStatType> callback) {
        for (Skill skill : Skill.values()) {
            String apiName = skill.getApiName() + "Points";

            SkillStatType statType = new SkillStatType(
                    "SKILL_" + skill.name(),
                    skill.getDisplayName(),
                    apiName,
                    apiName.toUpperCase(Locale.ROOT),
                    StatUnit.RAW,
                    skill);
            callback.accept(statType);
        }
    }
}
