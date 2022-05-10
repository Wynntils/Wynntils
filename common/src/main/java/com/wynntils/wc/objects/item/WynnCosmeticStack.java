/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.features.CosmeticHighlightFeature;
import com.wynntils.mc.utils.RenderUtils;
import com.wynntils.wc.objects.item.render.RenderedBackground;
import net.minecraft.ChatFormatting;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WynnCosmeticStack extends WynnItemStack implements RenderedBackground {

    private ChatFormatting color;

    public WynnCosmeticStack(ItemStack stack) {
        super(stack);

        color = ChatFormatting.getByCode(stack.getHoverName().getString().charAt(1));
        if (color == null) color = ChatFormatting.WHITE;
    }

    @Override
    public void renderBackground(PoseStack poseStack, Slot slot, Slot hovered) {
        int highlightColor = color.getColor();
        if (CosmeticHighlightFeature.highlightDuplicates
                && hovered != null
                && hovered != slot
                && hovered.getItem().getHoverName().equals(getHoverName())) {
            highlightColor = ChatFormatting.GREEN.getColor();
        }

        highlightColor = 0xFF000000 | highlightColor;
        RenderUtils.drawTexturedRectWithColor(
                RenderUtils.highlight, highlightColor, slot.x - 1, slot.y - 1, 18, 18, 256, 256);
    }
}
