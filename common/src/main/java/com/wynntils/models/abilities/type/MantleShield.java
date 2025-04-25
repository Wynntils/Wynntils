/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MantleShield extends ShieldType {
    private static final ClassType CLASS_TYPE = ClassType.WARRIOR;
    private static final SpellType SPELL_TYPE = SpellType.WAR_SCREAM;
    private static final String NAME = "Mantle";
    private static final int MANTLE_DAMAGE_VALUE = 62;
    private static final StyledText SHIELD_COOLDOWN_NAME = StyledText.fromString("§7Shield");

    public MantleShield() {
        super(CLASS_TYPE, SPELL_TYPE, NAME);
    }

    @Override
    protected boolean shouldClearOnSpellCast() {
        return Models.StatusEffect.getStatusEffects().stream()
                .noneMatch(statusEffect -> SHIELD_COOLDOWN_NAME.equals(statusEffect.getName()));
    }

    @Override
    protected boolean verifyArmorStand(ArmorStand armorStand) {
        ItemStack bootsItem = armorStand.getItemBySlot(EquipmentSlot.HEAD);
        return bootsItem.getItem().equals(Items.DIAMOND_AXE) && bootsItem.getDamageValue() == MANTLE_DAMAGE_VALUE;
    }
}
