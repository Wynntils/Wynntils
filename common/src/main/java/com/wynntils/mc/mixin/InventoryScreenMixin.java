/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ContainerRenderEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    private void renderPost(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
        MixinHelper.post(new ContainerRenderEvent(
                (AbstractContainerScreen<?>) (Object) this,
                guiGraphics,
                mouseX,
                mouseY,
                partialTicks,
                ((AbstractContainerScreen<?>) (Object) this).hoveredSlot));
    }
}
