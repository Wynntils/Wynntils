/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.OptionalDouble;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class CustomRenderType extends RenderType {
    // Copied from RenderType.LINE_STRIP and changed the line width from the default
    // to 3
    public static final RenderType LOOTRUN_LINE = RenderType.create(
            "wynntils_lootrun_line",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            Mode.LINE_STRIP,
            256,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new LineStateShard(OptionalDouble.of(3)))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    private static final Function<ResourceLocation, RenderType> POSITION_COLOR_TEXTURE =
            Util.memoize(resource -> RenderType.create(
                    "wynntils_position_color_texture",
                    DefaultVertexFormat.POSITION_COLOR_TEX,
                    Mode.QUADS,
                    256,
                    false,
                    false,
                    CompositeState.builder()
                            .setShaderState(POSITION_COLOR_TEX_SHADER)
                            .setTextureState(new TextureStateShard(resource, false, false))
                            .setTransparencyState(CustomRenderStateShard.SEMI_TRANSPARENT_TRANSPARENCY)
                            .setWriteMaskState(COLOR_WRITE)
                            .createCompositeState(false)));

    public static RenderType getPositionColorTexture(ResourceLocation resource) {
        return POSITION_COLOR_TEXTURE.apply(resource);
    }

    public CustomRenderType(
            String pName,
            VertexFormat pFormat,
            Mode pMode,
            int pBufferSize,
            boolean pAffectsCrumbling,
            boolean pSortOnUpload,
            Runnable pSetupState,
            Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }
}
