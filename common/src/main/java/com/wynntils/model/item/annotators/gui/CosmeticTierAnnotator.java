/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators.gui;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.model.item.gui.CosmeticItem;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CosmeticTierAnnotator implements ItemAnnotator {
    private static final Pattern COSMETIC_PATTERN =
            Pattern.compile("(Common|Rare|Epic|Godly|\\|\\|\\| Black Market \\|\\|\\|) Reward");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        if (!isCosmetic(itemStack)) return null;

        ChatFormatting chatColor =
                ChatFormatting.getByCode(itemStack.getHoverName().getString().charAt(1));
        if (chatColor == null) chatColor = ChatFormatting.WHITE;

        CustomColor highlightColor = CustomColor.fromChatFormatting(chatColor);
        return new CosmeticItem(highlightColor);
    }

    public static boolean isCosmetic(ItemStack itemStack) {
        for (Component c : ItemUtils.getTooltipLines(itemStack)) {
            if (COSMETIC_PATTERN.matcher(c.getString()).matches()) return true;
        }
        return false;
    }
}
