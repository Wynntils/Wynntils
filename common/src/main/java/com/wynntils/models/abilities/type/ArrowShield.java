/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;

public class ArrowShield extends ShieldType {
    private static final ClassType CLASS_TYPE = ClassType.ARCHER;
    private static final SpellType SPELL_TYPE = SpellType.ARROW_SHIELD;
    private static final String NAME = "Arrow";
    private static final float ARROW_SHIELD_DAMAGE_VALUE = 1411f; // Other possibilities: 1412f, 1410f, 1409f

    public ArrowShield() {
        super(CLASS_TYPE, SPELL_TYPE, NAME);
    }

    @Override
    protected boolean verifyEntity(Display.ItemDisplay itemDisplay) {
        ItemStack stack = itemDisplay.itemRenderState().itemStack();
        List<Float> floats = stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY).floats();
        return stack.getItem().equals(Items.OAK_BOAT) &&
                floats.contains(ARROW_SHIELD_DAMAGE_VALUE);
    }
}
