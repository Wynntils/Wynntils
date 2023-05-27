/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.gathering.ToolProfile;
import com.wynntils.models.items.items.game.GatheringToolItem;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.WynnItemMatchers;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class GatheringToolAnnotator implements ItemAnnotator {
    private static final Pattern GATHERING_TOOL_PATTERN =
            Pattern.compile("^§f[ⒸⒷⓀⒿ] Gathering (Axe|Rod|Scythe|Pickaxe) T(\\d+)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(GATHERING_TOOL_PATTERN);
        if (!matcher.matches()) return null;

        String toolType = matcher.group(1);
        int tier = Integer.parseInt(matcher.group(2));

        ToolProfile toolProfile = ToolProfile.fromString(toolType, tier);
        if (toolProfile == null) return null;

        CappedValue durability = WynnItemMatchers.getDurability(itemStack);

        return new GatheringToolItem(toolProfile, durability);
    }
}
