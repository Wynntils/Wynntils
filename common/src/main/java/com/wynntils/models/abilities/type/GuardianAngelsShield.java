/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuardianAngelsShield extends ShieldType {
    private static final ClassType CLASS_TYPE = ClassType.ARCHER;
    private static final SpellType SPELL_TYPE = SpellType.ARROW_SHIELD;
    private static final String NAME = "Guardian Angels";
    private static final int GUARDIAN_ANGEL_DAMAGE_VALUE = 7;

    public GuardianAngelsShield() {
        super(CLASS_TYPE, SPELL_TYPE, NAME);
    }

    @Override
    protected boolean verifyArmorStand(ArmorStand armorStand) {
        ItemStack headItem = armorStand.getItemBySlot(EquipmentSlot.HEAD);
        return headItem.getItem().equals(Items.DIAMOND_SWORD)
                && headItem.getDamageValue() == GUARDIAN_ANGEL_DAMAGE_VALUE;
    }
}
