/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.handleditems.items.game.XpPotionItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class XpPotionAnnotator implements ItemAnnotator {
    private static final Pattern XP_POTION_PATTERN = Pattern.compile("^§6Potion of Wisdom$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        String name = ComponentUtils.getCoded(itemStack.getHoverName());
        Matcher matcher = XP_POTION_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        return new XpPotionItem();
    }
}
