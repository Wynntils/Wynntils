/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.util.Optional;

public class GuardianAngelsShield extends ShieldType {
    private static final ClassType CLASS_TYPE = ClassType.ARCHER;
    private static final SpellType SPELL_TYPE = SpellType.ARROW_SHIELD;
    // change to private after spellevent supports ultimates
    public static final String GROUP = "Guardian Angels";
    public static final String ULT_GROUP = "Angelic Ascension Guardian Angels";
    private static final String NAME = "Guardian Angels";

    public GuardianAngelsShield() {
        super(CLASS_TYPE, SPELL_TYPE, NAME);
    }

    @Override
    protected boolean verifyCustomModelData(float customModelData) {
        Optional<String> group = Services.ItemDisplayModel.getGroup(customModelData);

        return group.isPresent() && (group.get().equals(GROUP) || group.get().equals(ULT_GROUP));
    }
}
