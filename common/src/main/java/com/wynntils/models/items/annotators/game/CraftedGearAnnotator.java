/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;

public final class CraftedGearAnnotator implements GameItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(WynnItemParser.CRAFTED_ITEM_NAME_PATTERN);
        if (!matcher.matches()) return null;

        return Models.Gear.parseCraftedGearItem(itemStack);
    }
}
