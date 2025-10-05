/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gambits.type.Gambit;
import com.wynntils.models.gambits.type.GambitStatus;
import com.wynntils.models.items.items.gui.GambitItem;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class GambitAnnotator implements GuiItemAnnotator {
    private static final Pattern NAME_PATTERN = Pattern.compile("^§(#[0-9A-Fa-f]{6,8})§l(.+? Gambit)$");

    private static final String GAMBIT_ENABLED = "Click to Disable";
    private static final String PLAYER_READY = "Un-ready to change";

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(NAME_PATTERN);
        if (!matcher.matches()) return null;

        CustomColor color = CustomColor.fromHexString(matcher.group(1));
        String itemName = matcher.group(2);
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        List<StyledText> description = extractDescriptionLines(lore);

        String loreStatus = lore.getLast().toString();
        GambitStatus gambitStatus;
        if (loreStatus.contains(GAMBIT_ENABLED)) {
            gambitStatus = GambitStatus.ENABLED;
        } else if (loreStatus.contains(PLAYER_READY)) {
            gambitStatus = GambitStatus.PLAYER_READY;
        } else {
            gambitStatus = GambitStatus.DISABLED;
        }

        return new GambitItem(Gambit.fromItemName(itemName), itemName, color, description, gambitStatus);
    }

    private List<StyledText> extractDescriptionLines(List<StyledText> lines) {
        return lines.stream()
                .dropWhile(line -> !line.trim().isEmpty())
                .skip(1)
                .takeWhile(line -> !line.trim().isEmpty())
                .toList();
    }
}
