/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin extends Screen {
    protected AbstractContainerScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;II)V", at = @At("RETURN"))
    public void renderTooltipPre(PoseStack poseStack, int mouseX, int mouseY, CallbackInfo info) {
        Screen screen = (Screen) (Object) this;

        EventFactory.onTooltipRender(screen, poseStack, mouseX, mouseY);
    }
}
