/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.screens.GearViewerScreen;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GearItemButton extends WynntilsButton {
    private final GearViewerScreen gearViewerScreen;
    private final ItemStack itemStack;

    public GearItemButton(int x, int y, int width, int height, GearViewerScreen gearViewerScreen, ItemStack itemStack) {
        super(x, y, width, height, Component.literal("Gear Item Button"));
        this.gearViewerScreen = gearViewerScreen;
        this.itemStack = itemStack;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (itemStack == null) return;

        RenderUtils.renderGuiItem(
                itemStack,
                (int) (gearViewerScreen.getTranslationX() + this.getX()),
                (int) (gearViewerScreen.getTranslationY() + this.getY()),
                1);
    }

    @Override
    public void onPress() {}

    public ItemStack getItemStack() {
        return itemStack;
    }
}
