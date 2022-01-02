/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
    @Shadow private Slot hoveredSlot;

    private AbstractContainerScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;II)V", at = @At("RETURN"))
    private void renderTooltipPre(PoseStack poseStack, int mouseX, int mouseY, CallbackInfo info) {
        Screen screen = (Screen) (Object) this;

        EventFactory.onTooltipRender(screen, poseStack, mouseX, mouseY);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", at = @At("RETURN"))
    private void renderPost(
            PoseStack client, int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
        Screen screen = (Screen) (Object) this;

        EventFactory.onInventoryRender(
                screen, client, mouseX, mouseY, partialTicks, this.hoveredSlot);
    }
}
