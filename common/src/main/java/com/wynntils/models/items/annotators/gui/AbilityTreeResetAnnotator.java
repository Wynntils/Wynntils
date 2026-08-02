/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.AbilityTreeResetItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class AbilityTreeResetAnnotator implements GuiItemAnnotator {
    private static final Pattern ABILITY_TREE_RESET_PATTERN =
            Pattern.compile("^(§8§lWaiting for Shards|§a§lConfirm Sacrifice)");

    private static final String WAITING_FOR_SHARDS = "§8§lWaiting for Shards";
    private static final String CONFIRM_SACRIFICE = "§a§lConfirm Sacrifice";

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(ABILITY_TREE_RESET_PATTERN);

        if (matcher.find()) {
            String result = matcher.group(1);

            if (result.equals(WAITING_FOR_SHARDS)) {
                return new AbilityTreeResetItem(false);
            } else if (result.equals(CONFIRM_SACRIFICE)) {
                return new AbilityTreeResetItem(true);
            }
        }

        return null;
    }
}
