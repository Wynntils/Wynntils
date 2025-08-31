/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.buffered;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard;

public abstract class CustomRenderStateShard extends RenderStateShard {
    protected static final TransparencyStateShard SEMI_TRANSPARENT_TRANSPARENCY = new TransparencyStateShard(
            "semi_transparent_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(
                        GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    protected CustomRenderStateShard(String string, Runnable runnable, Runnable runnable2) {
        super(string, runnable, runnable2);
    }
}
