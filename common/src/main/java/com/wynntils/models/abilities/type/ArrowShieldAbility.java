/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.util.List;
import java.util.Set;

public class ArrowShieldAbility extends CastedAbilityType {
    private static final ClassType CLASS_TYPE = ClassType.ARCHER;
    private static final SpellType SPELL_TYPE = SpellType.ARROW_SHIELD;
    private static final String NAME = "Arrow";
    private static final String GROUP = "Arrow Shield";

    public ArrowShieldAbility() {
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
    public Set<Class<? extends CastedAbilityType>> getConflictingTypes() {
        return Set.of(GuardianAngelsAbility.class);
    }

    @Override
    public boolean isShieldType() {
        return true;
    }
}
