/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.lootrun.objects;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class LootrunRenderType extends RenderType {

    // Copied from RenderType.LINE_STRIP and changed the line width from the default
    // to 3
    public static final RenderType LOOTRUN_LINE = RenderType.create(
            "lootrun",
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

    public LootrunRenderType(
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
