/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class CharmAnnotator implements GameItemAnnotator {
    private static final Pattern CHARM_PATTERN = Pattern.compile(
            "^\uDAFC\uDC00(?<unid>§f\uE008\uDB00\uDC02)?§[5bcdef](Charm of the (?<Type>\\w+))\uDAFC\uDC00$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(CHARM_PATTERN);
        if (!matcher.matches()) return null;

        String displayName = matcher.group(2);
        String type = matcher.group("Type");
        boolean isUnidentified = matcher.group("unid") != null;

        return Models.Rewards.fromCharmItemStack(itemStack, name, displayName, type, isUnidentified);
    }
}
