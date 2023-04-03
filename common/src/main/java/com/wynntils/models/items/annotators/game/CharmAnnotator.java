/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.utils.mc.type.StyledText;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class CharmAnnotator implements ItemAnnotator {
    private static final Pattern CHARM_PATTERN = Pattern.compile("^§[5abcdef](Charm of the (?<Type>\\w+))$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.match(CHARM_PATTERN);
        if (!matcher.matches()) return null;

        String displayName = matcher.group(1);
        String type = matcher.group("Type");

        return Models.Rewards.fromCharmItemStack(itemStack, name, displayName, type);
    }
}
