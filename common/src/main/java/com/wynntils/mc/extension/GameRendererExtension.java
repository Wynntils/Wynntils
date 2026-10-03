/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface GameRendererExtension {
    void setOverridenRenderTarget(RenderTarget renderTarget);
}
