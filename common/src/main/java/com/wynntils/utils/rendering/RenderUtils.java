/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.wynntils.utils.rendering.colors.CommonColors;
import com.wynntils.utils.rendering.colors.CustomColor;
import java.awt.*;
import java.util.Random;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL30;

public class RenderUtils {
    private static final Vector3f[] godRaysOffset = new Vector3f[] {
        new Vector3f(1, 0, 0),
        new Vector3f(0, 1, 0),
        new Vector3f(0, 0, 1),
        new Vector3f(1, 0, 0),
        new Vector3f(0, 1, 0),
        new Vector3f(0, 0, 1),
    };

    public static void renderGodRays(
            PoseStack poseStack, int x, int y, int z, double size, int rays, CustomColor color) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);

        float time = System.currentTimeMillis() / 50f;
        Random rand = new Random(142L);

        boolean isRainbow = color == CommonColors.RAINBOW;

        poseStack.pushPose();
        {
            { // gl setting
                RenderSystem.blendFunc(
                        GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.DestFactor.ONE.value);
                RenderSystem.enableBlend();
                RenderSystem.colorMask(true, true, true, false); // Disable alpha
                RenderSystem.disableCull();
                RenderSystem.disableTexture();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.depthMask(false);
            }

            // poseStack.translate(x, y, z);

            Matrix4f matrix = poseStack.last().pose();

            matrix.translate(new Vector3f(x, y, z));

            Vector4f a = new Vector4f();
            Vector4f b = new Vector4f();
            Vector4f c = new Vector4f();

            Vector3f rotationAxis = new Vector3f(0, 1, 0);

            for (int i = 0; i < rays; i++) {
                //                for (Vector3f vec : godRaysOffset) {
                //                    MatrixMathUtils.rotate((float) Math.PI * 2 * rand.nextFloat() + time / 360, vec,
                // matrix, matrix);
                //                }
                //
                float r = (1F + rand.nextFloat() * 2.5F) * 2;
                //
                //                MatrixMathUtils.rotate(time / 180f, rotationAxis, matrix, matrix);

                a.set(0F, 0.126f * r, 0.5f * r, 1);
                b.set(0F, -0.126f * r, 0.5f * r, 1);
                c.set(0F, 0, 0.6f * r, 1);

                a.transform(matrix);
                b.transform(matrix);
                c.transform(matrix);

                float red, green, blue;
                if (isRainbow) {
                    int rgb = Color.HSBtoRGB(i / 16f, 1, 1);

                    red = ((rgb & 0x00ff0000) >> 16) / 255.0f;
                    green = ((rgb & 0x0000ff00) >> 8) / 255.0f;
                    blue = ((rgb & 0x000000ff)) / 255.0f;
                } else {
                    red = color.r;
                    green = color.g;
                    blue = color.b;
                }

                Tesselator tess = Tesselator.getInstance();
                BufferBuilder builder = tess.getBuilder();
                {
                    builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    builder.vertex(size, size, size)
                            .color(red, green, blue, 0.9F)
                            .endVertex();
                    builder.vertex(a.x() * size, a.y() * size, a.z() * size)
                            .color(red, green, blue, 0.01F)
                            .endVertex();
                    builder.vertex(c.x() * size, c.y() * size, c.z() * size)
                            .color(red, green, blue, 0.01F)
                            .endVertex();
                    builder.vertex(b.x() * size, b.y() * size, b.z() * size)
                            .color(red, green, blue, 0.01F)
                            .endVertex();
                }

                builder.end();
                BufferUploader.end(builder);
            }

            { // gl resetting
                RenderSystem.enableTexture();
                RenderSystem.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
                RenderSystem.enableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.colorMask(true, true, true, true); // enable alpha
            }

            poseStack.translate(0.0D, 0.0D, 0.0D);
        }
        poseStack.popPose();
    }
}
