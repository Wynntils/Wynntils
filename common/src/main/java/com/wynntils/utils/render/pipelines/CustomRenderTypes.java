/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.pipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.wynntils.utils.render.Texture;
import java.util.function.Function;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.util.Util;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

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

    private static final Function<Identifier, RenderType> MAP_POSITION_TEXTURE_QUAD =
            Util.memoize(identifier -> RenderType.create(
                    "wynntils_map_position_texture_quad",
                    DefaultVertexFormat.POSITION_TEX,
                    Mode.QUADS,
                    256,
                    false,
                    false,
                    CompositeState.builder()
                            .setShaderState(POSITION_TEX_SHADER)
                            .setTextureState(new TextureStateShard(identifier, TriState.FALSE, false))
                            .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                            .setTexturingState(new TexturingStateShard(
                                    "map_clamping",
                                    () -> {
                                        RenderSystem.texParameter(
                                                GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                                        RenderSystem.texParameter(
                                                GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

                                        RenderSystem.texParameter(
                                                GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);
                                        RenderSystem.texParameter(
                                                GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);
                                    },
                                    () -> {
                                        // Hack: We should reset our texture parameters here,
                                        // but doing so causes weirdness when using Sodium
                                    }))
                            .createCompositeState(false)));

    public static RenderType getMapPositionTextureQuad(Identifier resource) {
        return MAP_POSITION_TEXTURE_QUAD.apply(resource);
    }
}
