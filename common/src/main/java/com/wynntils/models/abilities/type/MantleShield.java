/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import java.io.IOException;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

public class MantleShield extends ShieldType {
    private static final ClassType CLASS_TYPE = ClassType.WARRIOR;
    private static final SpellType SPELL_TYPE = SpellType.WAR_SCREAM;
    private static final String NAME = "Mantle";
    private static final int DIVIDE_BY = 3;
    private static final int MANTLE_TEXTURE_HASH = -2042749972; // 3 == 9, so div by 3
    private static final StyledText SHIELD_COOLDOWN_NAME = StyledText.fromString("§7Shield");

    public MantleShield() {
        super(CLASS_TYPE, SPELL_TYPE, NAME, DIVIDE_BY);
    }

    @Override
    protected boolean shouldClearOnSpellCast() {
        return Models.StatusEffect.getStatusEffects().stream()
                .noneMatch(statusEffect -> SHIELD_COOLDOWN_NAME.equals(statusEffect.getName()));
    }

    @Override
    protected boolean verifyEntity(Display.ItemDisplay itemDisplay) {
        ItemStack stack = itemDisplay.itemRenderState().itemStack();
        if (!stack.getItem().equals(Items.OAK_BOAT)) return false;
        List<Float> floats = stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY)
                .floats();
        try {
            return Services.CustomModel.getTextureHashes(floats).contains(MANTLE_TEXTURE_HASH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
