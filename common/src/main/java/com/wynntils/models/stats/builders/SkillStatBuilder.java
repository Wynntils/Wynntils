/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.builders;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatUnit;
import com.wynntils.utils.StringUtils;
import java.util.Locale;
import java.util.function.Consumer;

public final class SkillStatBuilder extends StatBuilder<SkillStatType> {
    @Override
    public void buildStats(Consumer<SkillStatType> callback) {
        for (Skill skill : Skill.values()) {
            String internalName = (skill.getApiName() + "Points").toUpperCase(Locale.ROOT);
            String apiName = "raw" + StringUtils.capitalized(skill.getApiName());

            SkillStatType statType = new SkillStatType(
                    "SKILL_" + skill.name(), skill.getDisplayName(), apiName, internalName, StatUnit.RAW, skill);
            callback.accept(statType);
        }
    }
}
