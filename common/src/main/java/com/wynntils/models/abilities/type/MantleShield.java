/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.util.Optional;

public class MantleShield extends ShieldType {
    private static final ClassType CLASS_TYPE = ClassType.WARRIOR;
    private static final SpellType SPELL_TYPE = SpellType.WAR_SCREAM;
    private static final String NAME = "Mantle";
    private static final String GROUP = "Mantle";

    public MantleShield() {
        super(CLASS_TYPE, SPELL_TYPE, NAME);
    }

    @Override
    protected boolean verifyCustomModelData(float customModelData) {
        Optional<String> group = Services.CustomModel.getGroup(customModelData);

        return group.isPresent() && group.get().equals(GROUP);
    }
}
