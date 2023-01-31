/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TomeAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        if (!(itemStack.getItem() == Items.ENCHANTED_BOOK)) return null;
        Matcher matcher = Models.Rewards.TOME_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        return Models.Rewards.fromTomeItemStack(itemStack, name, matcher);
    }
}
