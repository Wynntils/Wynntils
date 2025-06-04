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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class GambitAnnotator implements GuiItemAnnotator {
    private static final Pattern NAME_PATTERN = Pattern.compile("^§(#[0-9A-Fa-f]{6,8})§l(.+? Gambit)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(NAME_PATTERN);
        if (!matcher.matches()) return null;

        CustomColor color = CustomColor.fromHexString(matcher.group(1));
        String itemName = matcher.group(2);
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        List<StyledText> description = extractDescriptionLines(lore);

        return new GambitItem(itemName, color, description);
    }

    private List<StyledText> extractDescriptionLines(List<StyledText> lines) {
        return lines.stream()
                .dropWhile(line -> !line.trim().isEmpty())
                .skip(1)
                .takeWhile(line -> !line.trim().isEmpty())
                .toList();
    }
}
