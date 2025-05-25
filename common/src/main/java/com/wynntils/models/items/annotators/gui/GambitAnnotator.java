/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.GambitItem;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.LoreUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class GambitAnnotator implements GuiItemAnnotator {
    private static final Pattern NAME_PATTERN = Pattern.compile("^§(#[0-9A-Fa-f]{6,8})§l(.+? Gambit)$");
    private static final Pattern ASPECT_PULLS_PATTERN = Pattern.compile("^§b- §7Aspect Pulls: §f(\\d+)$");
    private static final Pattern REWARD_PULLS_PATTERN = Pattern.compile("^§b- §7Reward Pulls: §f(\\d+)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(NAME_PATTERN);
        if (!matcher.matches()) return null;
        CustomColor color = CustomColor.fromHexString(matcher.group(1));
        String itemName = matcher.group(2);

        Optional<Integer> startIndex = getRewardTitleStartLine(LoreUtils.getLore(itemStack));
        Optional<Integer> aspectCount = parseCount(itemStack, startIndex, ASPECT_PULLS_PATTERN);
        Optional<Integer> rewardCount = parseCount(itemStack, startIndex, REWARD_PULLS_PATTERN);

        if (aspectCount.isEmpty() || rewardCount.isEmpty()) return null;

        return new GambitItem(
                itemName,
                color,
                extractDescriptionLines(LoreUtils.getLore(itemStack)),
                aspectCount.get(),
                rewardCount.get());
    }

    private Optional<Integer> parseCount(ItemStack stack, Optional<Integer> startIndex, Pattern p) {
        if (startIndex.isEmpty()) return Optional.empty();

        Matcher m = LoreUtils.matchLoreLine(stack, startIndex.get(), p);
        return m.matches() ? Optional.of(Integer.parseInt(m.group(1))) : Optional.empty();
    }

    private Optional<Integer> getRewardTitleStartLine(List<StyledText> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getString().contains("Rewards for Enabling")) {
                return Optional.of(i); // description ends _before_ this.
            }
        }

        return Optional.empty();
    }

    private List<StyledText> extractDescriptionLines(List<StyledText> lines) {
        List<StyledText> description = new ArrayList<>();

        boolean reachedDescription = false;
        for (StyledText line : lines) {
            if (line.trim().isEmpty()) {
                if (!reachedDescription) {
                    reachedDescription = true;
                    continue;
                } else {
                    break;
                }
            }

            if (reachedDescription) {
                description.add(line);
            }
        }

        return description;
    }
}
