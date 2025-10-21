/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.buffered;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;

public class CustomRenderPipelines extends RenderPipelines {
    private static final RenderPipeline.Snippet POSITION_COLOR_QUAD_SNIPPET = RenderPipeline.builder(
                    MATRICES_PROJECTION_SNIPPET)
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBlend(CustomBlendFunction.SEMI_TRANSPARENT_BLEND_FUNCTION)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withDepthWrite(false)
            .buildSnippet();

    public static final RenderPipeline LOOTRUN_QUAD_PIPELINE =
            register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                    .withLocation("pipeline/wynntils_lootrun_quad")
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withSampler("Sampler0")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withCull(false)
                    .build());

    public static final RenderPipeline POSITION_COLOR_QUAD_PIPELINE =
            register(RenderPipeline.builder(POSITION_COLOR_QUAD_SNIPPET)
                    .withLocation("pipeline/wynntils_position_color_quad")
                    .withCull(false)
                    .build());
}
