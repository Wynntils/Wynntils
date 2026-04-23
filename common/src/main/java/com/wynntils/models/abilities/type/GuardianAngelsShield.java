/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;

import java.io.IOException;
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
    private static final int GUARDIAN_ANGEL_TEXTURE_HASH = 14170136; // 2 == 10, so div by 5

    public GuardianAngelsShield() {
        super(CLASS_TYPE, SPELL_TYPE, NAME);
    }

    @Override
    protected boolean verifyEntity(Display.ItemDisplay itemDisplay) {
        ItemStack stack = itemDisplay.itemRenderState().itemStack();
        if(!stack.getItem().equals(Items.OAK_BOAT)) return false;
        List<Float> floats = stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY)
                .floats();
        try {
            return Services.CustomModel.getTextureHashes(floats).contains(GUARDIAN_ANGEL_TEXTURE_HASH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
