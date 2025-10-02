/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.BaseEvent;
import net.minecraft.world.item.ItemStack;

public class GroundItemEntityTransformEvent extends BaseEvent {
    private final PoseStack poseStack;
    private final ItemStack itemStack;

    public GroundItemEntityTransformEvent(PoseStack poseStack, ItemStack itemStack) {
        this.poseStack = poseStack;
        this.itemStack = itemStack;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
