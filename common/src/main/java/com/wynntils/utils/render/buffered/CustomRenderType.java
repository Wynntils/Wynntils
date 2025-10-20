/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.buffered;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.wynntils.utils.render.Texture;
import java.util.OptionalDouble;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import com.wynntils.utils.render.Texture;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public abstract class CustomRenderType extends RenderType {
    // Copied from RenderType.LINE_STRIP and changed the line width from the default
    // to 3
    public static final RenderType LOOTRUN_LINE = RenderType.create(
            "wynntils_lootrun_line",
            1536,
            RenderPipelines.LINE_STRIP,
            RenderType.CompositeState.builder()
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3)))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .createCompositeState(false));

    public static final RenderType LOOTRUN_QUAD = RenderType.create(
            "wynntils_lootrun_quad",
            1536,
            CustomRenderPipelines.LOOTRUN_QUAD_PIPELINE,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(Texture.LOOTRUN_LINE.resource(), false))
                    .createCompositeState(false));

    public static final RenderType POSITION_COLOR_TRIANGLE_STRIP = RenderType.create(
            "wynntils_position_color_triangle_strip",
            DefaultVertexFormat.POSITION_COLOR,
            Mode.TRIANGLE_STRIP,
            256,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(false));

    public static final RenderType POSITION_COLOR_QUAD = RenderType.create(
            "wynntils_position_color_quad",
            DefaultVertexFormat.POSITION_COLOR,
            Mode.QUADS,
            256,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(CustomRenderStateShard.SEMI_TRANSPARENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    private static final Function<ResourceLocation, RenderType> POSITION_TEXTURE_QUAD =
            Util.memoize(resource -> RenderType.create(
                    "wynntils_position_texture_quad",
                    DefaultVertexFormat.POSITION_TEX,
                    Mode.QUADS,
                    256,
                    false,
                    false,
                    CompositeState.builder()
                            .setShaderState(POSITION_TEX_SHADER)
                            .setTextureState(new TextureStateShard(resource, TriState.FALSE, false))
                            .setTransparencyState(CustomRenderStateShard.SEMI_TRANSPARENT_TRANSPARENCY)
                            .createCompositeState(false)));

    private static final Function<ResourceLocation, RenderType> MAP_POSITION_TEXTURE_QUAD =
            Util.memoize(resource -> RenderType.create(
                    "wynntils_map_position_texture_quad",
                    DefaultVertexFormat.POSITION_TEX,
                    Mode.QUADS,
                    256,
                    false,
                    false,
                    CompositeState.builder()
                            .setShaderState(POSITION_TEX_SHADER)
                            .setTextureState(new TextureStateShard(resource, TriState.FALSE, false))
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

    private static final Function<ResourceLocation, RenderType> POSITION_COLOR_TEXTURE_QUAD =
            Util.memoize(resource -> RenderType.create(
                    "wynntils_position_color_texture_quad",
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    Mode.QUADS,
                    256,
                    false,
                    false,
                    CompositeState.builder()
                            .setShaderState(new ShaderStateShard(CoreShaders.POSITION_TEX_COLOR))
                            .setTextureState(new TextureStateShard(resource, TriState.FALSE, false))
                            .setTransparencyState(CustomRenderStateShard.SEMI_TRANSPARENT_TRANSPARENCY)
                            .setWriteMaskState(COLOR_WRITE)
                            .createCompositeState(false)));

    public static RenderType getPositionColorTextureQuad(ResourceLocation resource) {
        return POSITION_COLOR_TEXTURE_QUAD.apply(resource);
    }

    public static RenderType getPositionTextureQuad(ResourceLocation resource) {
        return POSITION_TEXTURE_QUAD.apply(resource);
    }

    public static RenderType getMapPositionTextureQuad(ResourceLocation resource) {
        return MAP_POSITION_TEXTURE_QUAD.apply(resource);
    }

    public CustomRenderType(
            String name,
            int bufferSize,
            boolean affectsCrumbling,
            boolean sortOnUpload,
            Runnable setupState,
            Runnable clearState) {
        super(name, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}
