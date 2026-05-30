/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.EmoteItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class EmoteAnnotator implements GuiItemAnnotator {
    private static final Pattern EMOTE_LORE_PATTERN = Pattern.compile("§aCommand: §7/emote §f([\\w_-]+)\\s*");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        if (lore.isEmpty()) return null;

        MatchResult matchResult =
                LoreUtils.matchLoreLine(itemStack, 4, EMOTE_LORE_PATTERN).toMatchResult();

        if (!matchResult.hasMatch()) return null;

        return new EmoteItem(itemStack.getCustomName().getString(), matchResult.group(1));
    }
}
