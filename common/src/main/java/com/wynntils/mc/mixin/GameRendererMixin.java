/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.wynntils.mc.extension.GameRendererExtension;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements GameRendererExtension {
    @Unique
    private RenderTarget wynntils_overridenRenderTarget;

    @WrapMethod(method = "mainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;")
    private RenderTarget mainRenderTarget(Operation<RenderTarget> operation) {
        if (this.wynntils_overridenRenderTarget != null) {
            return this.wynntils_overridenRenderTarget;
        }

        return operation.call();
    }

    @Override
    public void setOverridenRenderTarget(RenderTarget renderTarget) {
        this.wynntils_overridenRenderTarget = renderTarget;
    }
}
