/*
 * Copyright © Wynntils 2024-2025.
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
    private final String name;

    protected ShieldType(ClassType classType, SpellType spellType, String name) {
        this.validClass = classType;
        this.validSpell = spellType;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean validClass() {
        return Models.Character.getClassType() == validClass;
    }

    public boolean validSpell(SpellType spellType) {
        return spellType == validSpell && shouldClearOnSpellCast();
    }

    public boolean verifyShield(ArmorStand armorStand) {
        if (!validClass()) return false;

        return verifyArmorStand(armorStand);
    }

    // Arrow shield is part of the spell, so it will always be reset on the spell being cast
    // but shields such as Mantle shield is an ability added to the war scream spell, so it
    // is based on a cooldown rather than the spell itself
    protected boolean shouldClearOnSpellCast() {
        return true;
    }

    protected abstract boolean verifyArmorStand(ArmorStand armorStand);
}
