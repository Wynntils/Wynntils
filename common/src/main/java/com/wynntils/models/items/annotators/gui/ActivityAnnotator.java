/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.models.items.items.gui.ActivityItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ActivityAnnotator implements ItemAnnotator {
    private static final Pattern ACTIVITY_PATTERN = Pattern.compile("^§(?<color>.)(?<name>.+)§7 \\[(?<type>.+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.GOLDEN_AXE
                && itemStack.getItem() != Items.GOLDEN_PICKAXE
                && itemStack.getItem() != Items.GOLDEN_HOE) return null;

        Matcher matcher = name.getMatcher(ACTIVITY_PATTERN);
        if (!matcher.matches()) return null;

        ActivityType activityType = ActivityType.from(matcher.group("color"), matcher.group("type"));
        if (activityType == null) return null;

        String activityName = matcher.group("name");
        ActivityInfo activityInfo = Models.Activity.parseItem(activityName, activityType, itemStack);
        if (activityInfo == null) return null;

        return new ActivityItem(activityInfo);
    }
}
