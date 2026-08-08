/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.pipelines;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.wynntils.core.WynntilsMod;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

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

    // Reuses vanilla's core/position_tex_color vertex shader verbatim (plain pass-through of
    // position/UV/color) paired with a custom fragment shader that treats UV0 as normalized
    // [-1, 1] local position and masks it to an ellipse via a distance test. See
    // assets/wynntils/shaders/core/circle_mask.fsh and CircleMaskRenderState.
    public static final RenderPipeline CIRCLE_MASK_PIPELINE =
            register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                    .withLocation("pipeline/wynntils_circle_mask")
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "core/circle_mask"))
                    .withBlend(CustomBlendFunction.SEMI_TRANSPARENT_BLEND_FUNCTION)
                    .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                    .withDepthWrite(false)
                    .withCull(false)
                    .build());

    public static final RenderPipeline PROGRESS_BAR_PIPELINE =
            register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                    .withLocation("pipeline/wynntils_progress_bar")
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withSampler("Sampler0")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .build());
}
