/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.EmoteItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class EmoteAnnotator implements GuiItemAnnotator {
    private static final Pattern EMOTE_LORE_PATTERN = Pattern.compile("§aCommand: §7/emote §f([\\w_-]+)\\s*");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        if (lore.isEmpty()) return null;

        Matcher matcher = LoreUtils.matchLoreLine(itemStack, 4, EMOTE_LORE_PATTERN);

        if (!matcher.matches()) return null;

        return new EmoteItem(itemStack.getCustomName().getString().replace(" Emote", ""), matcher.group(1));
    }
}
