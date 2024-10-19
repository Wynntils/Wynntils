/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Models;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import net.minecraft.world.entity.decoration.ArmorStand;

public abstract class ShieldType {
    private final ClassType validClass;
    private final SpellType validSpell;

    protected ShieldType(ClassType classType, SpellType spellType) {
        this.validClass = classType;
        this.validSpell = spellType;
    }

    public boolean validClass() {
        return Models.Character.getClassType() == validClass;
    }

    public boolean validSpell(SpellType spellType) {
        return spellType == validSpell;
    }

    public boolean verifyShield(ArmorStand armorStand) {
        if (!validClass()) return false;

        return verifyArmorStand(armorStand);
    }

    protected abstract boolean verifyArmorStand(ArmorStand armorStand);
}
