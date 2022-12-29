/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.model.item.game.GatheringToolItem;
import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.profiles.ToolProfile;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class GatheringToolAnnotator implements ItemAnnotator {
    private static final Pattern GATHERING_TOOL_PATTERN =
            Pattern.compile("[ⒸⒷⓀⒿ] Gathering (Axe|Rod|Scythe|Pickaxe) T(\\d+)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        Matcher matcher = GATHERING_TOOL_PATTERN.matcher(
                WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(itemStack.getHoverName())));
        if (!matcher.matches()) return null;

        String toolType = matcher.group(1);
        int tier = Integer.parseInt(matcher.group(2));

        ToolProfile toolProfile = ToolProfile.fromString(toolType, tier);
        if (toolProfile == null) return null;

        CappedValue durability = WynnItemMatchers.getDurability(itemStack);

        return new GatheringToolItem(toolProfile, durability);
    }
}
