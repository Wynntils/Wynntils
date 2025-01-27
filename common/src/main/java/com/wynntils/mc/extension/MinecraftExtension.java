/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface MinecraftExtension {
    void setOverridenRenderTarget(RenderTarget renderTarget);
}
