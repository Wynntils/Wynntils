/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.items.game.GameItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class CraftedGearAnnotator extends GameItemAnnotator {
    private static final Pattern CRAFTED_GEAR_PATTERN = Pattern.compile("^§3(.*)§b \\[\\d{1,3}%\\]$");

    @Override
    public GameItem getAnnotation(ItemStack itemStack, StyledText name, int emeraldPrice) {
        Matcher matcher = name.getMatcher(CRAFTED_GEAR_PATTERN);
        if (!matcher.matches()) return null;

        return Models.Gear.parseCraftedGearItem(itemStack, emeraldPrice);
    }
}
