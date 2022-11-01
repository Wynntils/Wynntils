/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
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

    @ModifyArgs(
            method = "createBuffers",
            at =
            @At(
                    value = "INVOKE",
                    target =
                            "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
                    ordinal = 0,
                    remap = false))
    public void init(Args args) {
        args.set(2, GL30.GL_DEPTH32F_STENCIL8);
        args.set(6, GL30.GL_DEPTH_STENCIL);
        args.set(7, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV);
    }

    @ModifyArgs(
            method = "createBuffers",
            at =
            @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
                    remap = false),
            slice =
            @Slice(
                    from =
                    @At(
                            value = "FIELD",
                            target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;useDepth:Z",
                            ordinal = 1)))
    public void init2(Args args) {
        args.set(1, GL30.GL_DEPTH_STENCIL_ATTACHMENT);
    }
}
