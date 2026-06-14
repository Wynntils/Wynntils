/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.util.Collection;
import java.util.List;

public class JudrajimAbility extends CastedAbilityType {
    private static final ClassType CLASS_TYPE = ClassType.MAGE;
    private static final SpellType PARTIAL_SPELL_TYPE = SpellType.HEAL;
    private static final String NAME = "Judrajim";
    private static final String GROUP = "Judrajim";

    public JudrajimAbility() {
        super(CLASS_TYPE, null, PARTIAL_SPELL_TYPE, NAME);
    }

    @Override
    public boolean verifyCustomModelData(List<Float> modelIds) {
        if (modelIds.isEmpty()) return false;

        return modelIds.stream()
                .allMatch(f -> Services.CustomModel.getGroup(f)
                        .map(g -> g.equals(GROUP))
                        .orElse(false));
    }

    /**
     * This needs to be scheduled later, because the removal of the model and the cooldown need to overlap.
     */
    @Override
    public void onEntityRemoved(Collection<Integer> removedIds) {
        Managers.TickScheduler.scheduleLater(() -> super.onEntityRemoved(removedIds), 10);
    }
}
