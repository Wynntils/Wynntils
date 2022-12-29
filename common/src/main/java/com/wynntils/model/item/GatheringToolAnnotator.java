/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.model.item.game.ToolItem;
import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.profiles.ToolProfile;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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

        CappedValue durability = getDurability(itemStack);

        return new ToolItem(toolProfile, durability);
    }

    private CappedValue getDurability(ItemStack itemStack) {
        List<Component> lore = itemStack.getTooltipLines(null, TooltipFlag.NORMAL);
        for (Component line : lore) {
            Matcher durabilityMatcher = WynnItemMatchers.durabilityLineMatcher(line);
            if (!durabilityMatcher.find()) continue;

            var currentDurability = Integer.parseInt(durabilityMatcher.group(1));
            var maxDurability = Integer.parseInt(durabilityMatcher.group(2));
            return new CappedValue(currentDurability, maxDurability);
        }

        return CappedValue.EMPTY;
    }
}
