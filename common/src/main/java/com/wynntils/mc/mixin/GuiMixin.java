/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.RenderEvent;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void onRenderExperienceBarPre(PoseStack matrixStack, int xPos, CallbackInfo ci) {
        RenderEvent event = new RenderEvent.Pre(RenderEvent.ElementType.EXPERIENCE, matrixStack);
        WynntilsMod.getEventBus().post(event);

        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "renderExperienceBar", at = @At("RETURN"))
    public void onRenderExperienceBarPost(PoseStack matrixStack, int xPos, CallbackInfo ci) {
        RenderEvent event = new RenderEvent.Post(RenderEvent.ElementType.EXPERIENCE, matrixStack);
        WynntilsMod.getEventBus().post(event);
    }
}
