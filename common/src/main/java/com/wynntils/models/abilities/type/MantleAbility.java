/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.util.List;

public class MantleAbility extends CastedAbilityType {
    private static final ClassType CLASS_TYPE = ClassType.WARRIOR;
    private static final SpellType SPELL_TYPE = SpellType.WAR_SCREAM;
    private static final String NAME = "Mantle";
    private static final String GROUP = "Mantle";

    public MantleAbility() {
        super(CLASS_TYPE, SPELL_TYPE, null, NAME);
    }

    @Override
    public boolean verifyCustomModelData(List<Float> modelIds) {
        if (modelIds.isEmpty()) return false;

        return modelIds.stream()
                .allMatch(f -> Services.CustomModel.getGroup(f)
                        .map(g -> g.equals(GROUP))
                        .orElse(false));
    }

    @Override
    public boolean isShieldType() {
        return true;
    }
}
