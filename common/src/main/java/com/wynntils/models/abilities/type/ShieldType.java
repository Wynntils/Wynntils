/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Models;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;

public abstract class ShieldType {
    private final ClassType validClass;
    private final SpellType validSpell;
    private final String name;

    protected ShieldType(ClassType classType, SpellType spellType, String name) {
        this.validClass = classType;
        this.validSpell = spellType;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private boolean validClass() {
        return Models.Character.getClassType() == validClass;
    }

    public boolean validSpell(SpellType spellType) {
        return spellType == validSpell;
    }

    public boolean verifyShield(float customModelData) {
        if (!validClass()) return false;

        return verifyCustomModelData(customModelData);
    }

    protected abstract boolean verifyCustomModelData(float customModelData);
}
