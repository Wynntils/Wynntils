/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.GambitItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class GambitAnnotator implements GuiItemAnnotator {
    private static final Pattern NAME_PATTERN = Pattern.compile("^(.+?)'s Gambit$");
    private static final Pattern ASPECT_PULLS_PATTERN = Pattern.compile("§b- §7Aspect Pulls: §f(\\d+)$");
    private static final Pattern REWARD_PULLS_PATTERN = Pattern.compile("§b- §7Reward Pulls: §f(\\d+)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(NAME_PATTERN);
        if (!matcher.matches()) return null;
        String itemName = matcher.group(1);

        OptionalInt aspectCount = parseCount(itemStack, ASPECT_PULLS_PATTERN);
        OptionalInt rewardCount = parseCount(itemStack, REWARD_PULLS_PATTERN);
        if (aspectCount.isEmpty() || rewardCount.isEmpty()) return null;

        return new GambitItem(itemName, LoreUtils.getLore(itemStack), aspectCount.getAsInt(), rewardCount.getAsInt());
    }

    private OptionalInt parseCount(ItemStack stack, Pattern p) {
        Matcher m = LoreUtils.matchLoreLine(stack, 2, p);
        return m.matches() ? OptionalInt.of(Integer.parseInt(m.group(1))) : OptionalInt.empty();
    }
}
