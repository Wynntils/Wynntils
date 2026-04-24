/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.WardItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class WardAnnotator implements ItemAnnotator {
    private static final String ITEM_UPGRADER_LORE = "Item Upgrader";

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.POTION) return null;

        String plainName = name.getString();
        if (!plainName.contains("Ward")) return null;

        List<StyledText> lore = LoreUtils.getLore(itemStack);
        boolean isWard = false;
        for (StyledText line : lore) {
            if (line.getString().contains(ITEM_UPGRADER_LORE)) {
                isWard = true;
                break;
            }
        }

        if (!isWard) return null;

        return new WardItem(name);
    }
}
