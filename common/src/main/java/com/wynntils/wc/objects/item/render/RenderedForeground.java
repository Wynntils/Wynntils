/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.item.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.inventory.Slot;

public interface RenderedForeground {
    void renderForeground(PoseStack poseStack, Slot slot, Slot hovered);
}
