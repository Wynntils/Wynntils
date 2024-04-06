/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.models.elements.type.Skill;

public final class SkillStatType extends StatType {
    private final Skill skill;

    public SkillStatType(
            String key, String displayName, String apiName, String internalRollName, StatUnit unit, Skill skill) {
        super(key, displayName, apiName, internalRollName, unit);
        this.skill = skill;
    }

    public Skill getSkill() {
        return skill;
    }
}
