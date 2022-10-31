package com.wynntils.forge.mixins;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.wynntils.gui.render.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/*
 * While we could use the same implementation as https://gist.github.com/burgerguy/8233170683ad93eea6aa27ee02a5c4d1,
 * forge already has an implementation for stencils.
 */
@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin {

    @Shadow
    public abstract void enableStencil();

    @ModifyArgs(method = "createBuffers",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
                    ordinal = 0))
    public void init(Args args) {
        if (RenderUtils.isStencilEnabled()) {
            enableStencil();
        }
    }

}