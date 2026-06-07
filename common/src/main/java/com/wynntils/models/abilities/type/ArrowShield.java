/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.util.Optional;

public class ArrowShield extends ShieldType {
    private static final ClassType CLASS_TYPE = ClassType.ARCHER;
    private static final SpellType SPELL_TYPE = SpellType.ARROW_SHIELD;
    private static final String NAME = "Arrow";
    private static final String GROUP = "Arrow Shield";

    public ArrowShield() {
        super(CLASS_TYPE, SPELL_TYPE, NAME);
    }

    @Override
    protected boolean verifyCustomModelData(float customModelData) {
        Optional<String> group = Services.ItemDisplayModel.getGroup(customModelData);

        return group.isPresent() && group.get().equals(GROUP);
    }
}
