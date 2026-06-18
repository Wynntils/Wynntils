/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.util.Collection;
import java.util.List;

public class BrokenMantleAbility extends CastedAbilityType implements ShieldAbilityProperty {
    private static final ClassType CLASS_TYPE = ClassType.WARRIOR;
    private static final SpellType SPELL_TYPE = SpellType.WAR_SCREAM;
    private static final String NAME = "Broken Mantle";
    private static final String GROUP = "Broken Mantle";

    private final MantleAbility mantleAbility;

    public BrokenMantleAbility(MantleAbility mantleAbility) {
        super(CLASS_TYPE, SPELL_TYPE, null, NAME, GROUP);
        this.mantleAbility = mantleAbility;
    }

    @Override
    public boolean allowsOutOfWindowSpawn(List<Float> modelIds) {
        if (!mantleAbility.isActive()) return false;

        return verifyCustomModelData(modelIds);
    }

    @Override
    public void onMatched(int entityId, List<Float> modelIds) {
        // No registration delay needed
        entityIds.add(entityId);
    }

    @Override
    public void onEntityRemoved(Collection<Integer> removedIds) {
        entityIds.removeAll(removedIds);
        if (entityIds.isEmpty()) {
            onCleared();
        }
    }
}
