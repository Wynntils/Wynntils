/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item;

import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.custom.item.render.HighlightedItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CosmeticItemStack extends WynnItemStack implements HighlightedItem {

    private CustomColor highlightColor;

    public CosmeticItemStack(ItemStack stack) {
        super(stack);

        ChatFormatting chatColor =
                ChatFormatting.getByCode(stack.getHoverName().getString().charAt(1));
        if (chatColor == null) chatColor = ChatFormatting.WHITE;

        highlightColor = CustomColor.fromChatFormatting(chatColor);
    }

    @Override
    public CustomColor getHighlightColor(Screen screen, Slot slot) {
        return highlightColor;
    }
}
