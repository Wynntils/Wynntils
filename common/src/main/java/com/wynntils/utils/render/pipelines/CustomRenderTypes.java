/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.pipelines;

import com.wynntils.utils.render.Texture;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class CustomRenderTypes {
    public static final RenderType LOOTRUN_QUAD = RenderType.create(
            "wynntils_lootrun_quad",
            RenderSetup.builder(CustomRenderPipelines.LOOTRUN_QUAD_PIPELINE)
                    .withTexture("Sampler0", Texture.LOOTRUN_LINE.identifier())
                    .createRenderSetup());

    public static final RenderType POSITION_COLOR_QUAD = RenderType.create(
            "wynntils_position_color_quad",
            RenderSetup.builder(CustomRenderPipelines.POSITION_COLOR_QUAD_PIPELINE)
                    .createRenderSetup());
}
