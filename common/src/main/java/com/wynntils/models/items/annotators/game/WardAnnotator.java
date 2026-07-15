/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.game.WardItem;
import com.wynntils.models.rewards.type.WardType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class WardAnnotator implements GameItemAnnotator {
    private static final Pattern WARD_PATTERN =
            Pattern.compile("\uDAFC\uDC00§#[0-9a-fA-F]{8}([A-Z][a-z]*) Ward\uDAFC\uDC00");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher m = name.getMatcher(WARD_PATTERN);
        if (!m.matches()) return null;

        return new WardItem(WardType.fromName(m.group(1)));
    }
}
