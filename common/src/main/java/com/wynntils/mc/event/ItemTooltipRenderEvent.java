/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ItemTooltipRenderEvent extends Event {
    private final PoseStack poseStack;
    private final ItemStack itemStack;
    private final int mouseX;
    private final int mouseY;

    public ItemTooltipRenderEvent(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY) {
        this.poseStack = poseStack;
        this.itemStack = itemStack;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }
}
