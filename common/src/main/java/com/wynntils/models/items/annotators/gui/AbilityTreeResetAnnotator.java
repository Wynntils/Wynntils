/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.game.AbilityTreeResetItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class AbilityTreeResetAnnotator implements GameItemAnnotator {
    private static final Pattern ABILITY_TREE_RESET_PATTERN =
            Pattern.compile("^(§8§lWaiting for Shards|§a§lConfirm Sacrifice)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(ABILITY_TREE_RESET_PATTERN);

        if (matcher.find()) {
            String result = matcher.group(1);

            if (result.equals("§8§lWaiting for Shards")) {
                return new AbilityTreeResetItem(false);
            } else if (result.equals("§a§lConfirm Sacrifice")) {
                return new AbilityTreeResetItem(true);
            }
        }

        return null;
    }
}
