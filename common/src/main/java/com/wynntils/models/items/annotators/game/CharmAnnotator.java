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

public final class CharmAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher matcher = Models.Rewards.CHARM_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        return Models.Rewards.fromCharmItemStack(itemStack, name, matcher);
    }
}
