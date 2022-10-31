package com.wynntils.fabric.mixins;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.wynntils.gui.render.RenderUtils;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/*
 * See https://gist.github.com/burgerguy/8233170683ad93eea6aa27ee02a5c4d1
 */
@Mixin(RenderTarget.class)
public class RenderTargetMixin {
    @ModifyArgs(method = "createBuffers",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
                    ordinal = 0))
    public void init(Args args) {
        if (RenderUtils.isStencilEnabled()) {
            args.set(2, GL30.GL_DEPTH32F_STENCIL8);
            args.set(6, GL30.GL_DEPTH_STENCIL);
            args.set(7, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV);
        }
    }

    @ModifyArgs (method = "createBuffers",
            at = @At (value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;framebufferTexture2D(IIIII)V"),
            slice = @Slice(from = @At (value = "FIELD", target = "Lnet/minecraft/client/gl/Framebuffer;useDepthAttachment:Z", ordinal = 1)))
    public void init2(Args args) {
        if (RenderUtils.isStencilEnabled()) {
            args.set(1, GL30.GL_DEPTH_STENCIL_ATTACHMENT);
        }
    }
}