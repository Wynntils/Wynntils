/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.pipelines;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.pipeline.RenderPipelines;

public class CustomGuiPipelines {
    public static final GuiPipeline POSITION_COLOR_QUAD_PIPELINE = GuiPipeline.of(() -> RenderPipelines.dynamicPipeline(
            null,
            "wynntils_gui_position_color_quad",
            _ -> RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                    .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                    .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                    .withLocation("pipeline/wynntils_gui_position_color_quad")
                    .withVertexShader("core/gui")
                    .withFragmentShader("core/gui")
                    .withColorTargetState(new ColorTargetState(CustomBlendFunction.SEMI_TRANSPARENT_BLEND_FUNCTION))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withCull(false)
                    .build()));

    public static final GuiPipeline PROGRESS_BAR_PIPELINE = GuiPipeline.of(() -> RenderPipelines.dynamicPipeline(
            null,
            "wynntils_progress_bar",
            _ -> RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                    .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                    .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                    .withLocation("pipeline/wynntils_progress_bar")
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withCull(false)
                    .build()));
}
