/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge.mixins;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.wynntils.gui.render.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

/*
 * While we could use the same implementation as https://gist.github.com/burgerguy/8233170683ad93eea6aa27ee02a5c4d1,
 * forge already has an implementation for stencils.
 */
@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin {

    @Shadow(remap = false)
    public abstract void enableStencil();

    @Inject(method = "createBuffers", at = @At("HEAD"))
    public void init() {
        if (RenderUtils.isStencilEnabled()) {
            enableStencil();
        }
    }
}
