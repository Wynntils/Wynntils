/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeType;
import com.wynntils.models.items.items.gui.AbilityTreeNodeItem;
import com.wynntils.utils.type.IterationDecision;
import net.minecraft.world.item.ItemStack;

public class AbilityTreeNodeAnnotator implements GuiItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        AbilityTreeNodeType abilityTreeNodeType = AbilityTreeNodeType.fromItemStack(itemStack);
        if (abilityTreeNodeType == null) return null;

        StyledText actualName;
        if (name.getPartCount() == 1) {
            actualName = name;
        } else {
            actualName = name.iterate((part, changes) -> {
                // The part which is bolded is the actual name of the ability
                if (!part.getPartStyle().isBold()) {
                    changes.clear();
                }

                return IterationDecision.CONTINUE;
            });
        }

        return new AbilityTreeNodeItem(actualName, abilityTreeNodeType);
    }
}
