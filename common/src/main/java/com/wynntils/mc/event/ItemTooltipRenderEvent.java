/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class ItemTooltipRenderEvent extends Event {
    private final PoseStack poseStack;
    private ItemStack itemStack;
    private int mouseX;
    private int mouseY;

    protected ItemTooltipRenderEvent(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY) {
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

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

    @Cancelable
    public static class Pre extends ItemTooltipRenderEvent {
        private List<Component> tooltips;

        public Pre(PoseStack poseStack, ItemStack itemStack, List<Component> tooltips, int mouseX, int mouseY) {
            super(poseStack, itemStack, mouseX, mouseY);
            setTooltips(tooltips);
        }

        public List<Component> getTooltips() {
            return tooltips;
        }

        public void setTooltips(List<Component> tooltips) {
            this.tooltips = Collections.unmodifiableList(tooltips);
        }
    }

    public static class Post extends ItemTooltipRenderEvent {
        public Post(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY) {
            super(poseStack, itemStack, mouseX, mouseY);
        }
    }
}
