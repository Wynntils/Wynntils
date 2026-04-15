/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

public class GuardianAngelsShield extends ShieldType {
    private static final ClassType CLASS_TYPE = ClassType.ARCHER;
    private static final SpellType SPELL_TYPE = SpellType.ARROW_SHIELD;
    private static final String NAME = "Guardian Angels";
    private static final float GUARDIAN_ANGEL_DAMAGE_VALUE = 11870f; // Other possibilities: 11869f, 11868f, 11871f, 11872f

    public GuardianAngelsShield() {
        super(CLASS_TYPE, SPELL_TYPE, NAME);
    }

    @Override
    protected boolean verifyEntity(Display.ItemDisplay itemDisplay) {
        ItemStack stack = itemDisplay.itemRenderState().itemStack();
        List<Float> floats = stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY).floats();
        return stack.getItem().equals(Items.OAK_BOAT) &&
                floats.contains(GUARDIAN_ANGEL_DAMAGE_VALUE);
    }
}
