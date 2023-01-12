/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.mc.objects.CustomColor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class BufferedRenderUtils {
    public static void drawTexturedRect(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            ResourceLocation tex,
            CustomColor color,
            float alpha,
            float x,
            float y,
            float z,
            float width,
            float height) {

        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer buffer = bufferSource.getBuffer(CustomRenderType.getPositionColorTexture(tex));

        float[] colorArray = color.asFloatArray();

        buffer.vertex(matrix, x, y + height, z)
                .color(colorArray[0], colorArray[1], colorArray[2], alpha)
                .uv(0, 1)
                .endVertex();
        buffer.vertex(matrix, x + width, y + height, z)
                .color(colorArray[0], colorArray[1], colorArray[2], alpha)
                .uv(1, 1)
                .endVertex();
        buffer.vertex(matrix, x + width, y, z)
                .color(colorArray[0], colorArray[1], colorArray[2], alpha)
                .uv(1, 0)
                .endVertex();
        buffer.vertex(matrix, x, y, z)
                .color(colorArray[0], colorArray[1], colorArray[2], alpha)
                .uv(0, 0)
                .endVertex();
    }
}
