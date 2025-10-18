/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.pipelines.CustomRenderPipelines;
import com.wynntils.utils.render.state.ArcRenderState;
import com.wynntils.utils.render.state.CustomRectangleRenderState;
import com.wynntils.utils.render.state.DiagonalColoredRectangleRenderState;
import com.wynntils.utils.render.state.FloatBlitRenderState;
import com.wynntils.utils.render.state.FloatColoredRectangleRenderState;
import com.wynntils.utils.render.state.MulticoloredRectangleRenderState;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.RenderDirection;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class RenderUtils {
    // used to render player nametags as semi-transparent
    private static final int NAMETAG_COLOR = 0x80FFFFFF;

    public static void drawLine(
            GuiGraphics guiGraphics, CustomColor color, float x1, float y1, float x2, float y2, float width) {
        // Vertical or horizontal line
        if (x1 == x2 || y1 == y2) {
            float halfWidth = width / 2f;
            fill(
                    guiGraphics,
                    color,
                    Math.min(x1, x2) - (x1 == x2 ? halfWidth : 0),
                    Math.min(y1, y2) - (y1 == y2 ? halfWidth : 0),
                    Math.max(x1, x2) + (x1 == x2 ? halfWidth : 0),
                    Math.max(y1, y2) + (y1 == y2 ? halfWidth : 0));
            return;
        }

        // Diagonal line
        guiGraphics.guiRenderState.submitGuiElement(new DiagonalColoredRectangleRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(guiGraphics.pose()),
                x1,
                y1,
                x2,
                y2,
                width,
                color,
                guiGraphics.scissorStack.peek()));
    }

    public static void drawRect(
            GuiGraphics guiGraphics, CustomColor color, float x, float y, float width, float height) {
        fill(guiGraphics, color, x, y, x + width, y + height);
    }

    public static void drawRectBorders(
            GuiGraphics guiGraphics, CustomColor color, float x1, float y1, float x2, float y2, float lineWidth) {
        drawLine(guiGraphics, color, x1, y1, x2, y1, lineWidth);
        drawLine(guiGraphics, color, x2, y1, x2, y2, lineWidth);
        drawLine(guiGraphics, color, x2, y2, x1, y2, lineWidth);
        drawLine(guiGraphics, color, x1, y2, x1, y1, lineWidth);
    }

    public static void drawRotatingBorderSegment(
            GuiGraphics guiGraphics,
            CustomColor color,
            float x1,
            float y1,
            float x2,
            float y2,
            float lineWidth,
            float segmentFraction) {
        segmentFraction = MathUtils.clamp(segmentFraction, 0.0f, 1.0f);

        if (x2 < x1) {
            float tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        if (y2 < y1) {
            float tmp = y1;
            y1 = y2;
            y2 = tmp;
        }

        float width = x2 - x1;
        float height = y2 - y1;
        float perimeter = 2 * (width + height);

        float progress = (McUtils.player().tickCount % 100) / 100f;
        float segmentLength = segmentFraction * perimeter;
        float offset = (progress % 1.0f) * perimeter;

        float[][] points = {
            {x1, y1, x2, y1},
            {x2, y1, x2, y2},
            {x2, y2, x1, y2},
            {x1, y2, x1, y1}
        };

        float remainingLength = segmentLength;
        float accumulatedLength = 0f;

        for (int i = 0; i < points.length; i++) {
            float[] edge = points[i];
            float edgeX1 = edge[0];
            float edgeY1 = edge[1];
            float edgeX2 = edge[2];
            float edgeY2 = edge[3];
            float edgeLength = (float) Math.hypot(edgeX2 - edgeX1, edgeY2 - edgeY1);

            if (offset < accumulatedLength + edgeLength) {
                float localOffset = offset - accumulatedLength;
                float segmentEdgeStart = localOffset / edgeLength;
                float startX = edgeX1 + (edgeX2 - edgeX1) * segmentEdgeStart;
                float startY = edgeY1 + (edgeY2 - edgeY1) * segmentEdgeStart;

                while (remainingLength > 0) {
                    float segmentEdgeEnd = Math.min(1f, segmentEdgeStart + (remainingLength / edgeLength));
                    float endX = edgeX1 + (edgeX2 - edgeX1) * segmentEdgeEnd;
                    float endY = edgeY1 + (edgeY2 - edgeY1) * segmentEdgeEnd;

                    drawLine(guiGraphics, color, startX, startY, endX, endY, lineWidth);

                    remainingLength -= (segmentEdgeEnd - segmentEdgeStart) * edgeLength;

                    if (segmentEdgeEnd >= 1f) {
                        i = (i + 1) % points.length;
                        edge = points[i];
                        edgeX1 = edge[0];
                        edgeY1 = edge[1];
                        edgeX2 = edge[2];
                        edgeY2 = edge[3];

                        edgeLength = (float) Math.hypot(edgeX2 - edgeX1, edgeY2 - edgeY1);
                        segmentEdgeStart = 0f;

                        startX = edgeX1;
                        startY = edgeY1;
                    } else {
                        break;
                    }
                }
                break;
            }

            accumulatedLength += edgeLength;
        }
    }

    public static void fill(GuiGraphics guiGraphics, CustomColor color, float x1, float y1, float x2, float y2) {
        if (x1 > x2) {
            float t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y1 > y2) {
            float t = y1;
            y1 = y2;
            y2 = t;
        }

        guiGraphics.guiRenderState.submitGuiElement(new FloatColoredRectangleRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(guiGraphics.pose()),
                x1,
                y1,
                x2,
                y2,
                color,
                color,
                guiGraphics.scissorStack.peek()));
    }

    public static void drawTexturedRect(
            GuiGraphics guiGraphics,
            RenderPipeline pipeline,
            Identifier identifier,
            CustomColor color,
            float x,
            float y,
            float width,
            float height,
            float uOffset,
            float vOffset,
            float u,
            float v,
            int textureWidth,
            int textureHeight) {
        AbstractTexture abstractTexture = McUtils.mc().getTextureManager().getTexture(identifier);
        guiGraphics.guiRenderState.submitGuiElement(new FloatBlitRenderState(
                pipeline,
                TextureSetup.singleTexture(abstractTexture.getTextureView(), abstractTexture.getSampler()),
                new Matrix3x2f(guiGraphics.pose()),
                x,
                y,
                x + width,
                y + height,
                uOffset / textureWidth,
                (uOffset + u) / textureWidth,
                vOffset / textureHeight,
                (vOffset + v) / textureHeight,
                color,
                guiGraphics.scissorStack.peek()));
    }

    public static void drawTexturedRect(
            GuiGraphics guiGraphics,
            Identifier identifier,
            CustomColor color,
            float x,
            float y,
            float width,
            float height,
            float uOffset,
            float vOffset,
            float u,
            float v,
            int textureWidth,
            int textureHeight) {
        drawTexturedRect(
                guiGraphics,
                RenderPipelines.GUI_TEXTURED,
                identifier,
                color,
                x,
                y,
                width,
                height,
                uOffset,
                vOffset,
                u,
                v,
                textureWidth,
                textureHeight);
    }

    public static void drawTexturedRect(
            GuiGraphics guiGraphics,
            Texture texture,
            CustomColor color,
            float x,
            float y,
            float width,
            float height,
            float uOffset,
            float vOffset,
            float u,
            float v,
            int textureWidth,
            int textureHeight) {
        drawTexturedRect(
                guiGraphics,
                texture.identifier(),
                color,
                x,
                y,
                width,
                height,
                uOffset,
                vOffset,
                u,
                v,
                textureWidth,
                textureHeight);
    }

    public static void drawTexturedRect(
            GuiGraphics guiGraphics,
            Texture texture,
            float x,
            float y,
            float width,
            float height,
            float uOffset,
            float vOffset,
            float u,
            float v,
            int textureWidth,
            int textureHeight) {
        drawTexturedRect(
                guiGraphics,
                texture.identifier(),
                CustomColor.NONE,
                x,
                y,
                width,
                height,
                uOffset,
                vOffset,
                u,
                v,
                textureWidth,
                textureHeight);
    }

    public static void drawTexturedRect(
            GuiGraphics guiGraphics,
            Identifier identifier,
            float x,
            float y,
            float width,
            float height,
            float uOffset,
            float vOffset,
            float u,
            float v,
            int textureWidth,
            int textureHeight) {
        drawTexturedRect(
                guiGraphics,
                identifier,
                CustomColor.NONE,
                x,
                y,
                width,
                height,
                uOffset,
                vOffset,
                u,
                v,
                textureWidth,
                textureHeight);
    }

    public static void drawTexturedRect(
            GuiGraphics guiGraphics,
            Texture texture,
            float x,
            float y,
            float uOffset,
            float vOffset,
            float u,
            float v) {
        drawTexturedRect(
                guiGraphics,
                texture,
                CustomColor.NONE,
                x,
                y,
                texture.width(),
                texture.height(),
                uOffset,
                vOffset,
                u,
                v,
                texture.width(),
                texture.height());
    }

    public static void drawTexturedRect(GuiGraphics guiGraphics, Texture texture, CustomColor color, float x, float y) {
        drawTexturedRect(
                guiGraphics,
                texture,
                color,
                x,
                y,
                texture.width(),
                texture.height(),
                0,
                0,
                texture.width(),
                texture.height(),
                texture.width(),
                texture.height());
    }

    public static void drawTexturedRect(
            GuiGraphics guiGraphics,
            Identifier identifier,
            CustomColor color,
            float x,
            float y,
            float width,
            float height,
            int textureWidth,
            int textureHeight) {
        drawTexturedRect(
                guiGraphics,
                identifier,
                color,
                x,
                y,
                width,
                height,
                0,
                0,
                textureWidth,
                textureHeight,
                textureWidth,
                textureHeight);
    }

    public static void drawTexturedRect(GuiGraphics guiGraphics, Texture texture, float x, float y) {
        drawTexturedRect(guiGraphics, texture, CustomColor.NONE, x, y);
    }

    public static void drawScalingTexturedRect(
            GuiGraphics guiGraphics,
            Identifier identifier,
            float x,
            float y,
            float width,
            float height,
            int textureWidth,
            int textureHeight) {
        drawTexturedRect(
                guiGraphics,
                identifier,
                CustomColor.NONE,
                x,
                y,
                width,
                height,
                0,
                0,
                textureWidth,
                textureHeight,
                textureWidth,
                textureHeight);
    }

    public static void drawScalingTexturedRect(
            GuiGraphics guiGraphics, Texture texture, CustomColor color, float x, float y, float width, float height) {
        drawScalingTexturedRect(
                guiGraphics, texture.identifier(), color, x, y, width, height, texture.width(), texture.height());
    }

    public static void drawScalingTexturedRect(
            GuiGraphics guiGraphics, Texture texture, float x, float y, float width, float height) {
        drawScalingTexturedRect(
                guiGraphics, texture.identifier(), x, y, width, height, texture.width(), texture.height());
    }

    public static void drawScalingTexturedRect(
            GuiGraphics guiGraphics,
            Identifier identifier,
            CustomColor color,
            float x,
            float y,
            float width,
            float height,
            int textureWidth,
            int textureHeight) {
        drawTexturedRect(
                guiGraphics,
                identifier,
                color,
                x,
                y,
                width,
                height,
                0,
                0,
                textureWidth,
                textureHeight,
                textureWidth,
                textureHeight);
    }

    public static void drawHoverableTexturedRect(
            GuiGraphics guiGraphics, Texture texture, float x, float y, boolean hovered, RenderDirection dir) {
        int textureWidth = texture.width();
        int textureHeight = texture.height();

        int renderWidth = (dir == RenderDirection.HORIZONTAL ? textureWidth / 2 : textureWidth);
        int renderHeight = (dir == RenderDirection.VERTICAL ? textureHeight / 2 : textureHeight);

        float uOffset = (hovered && dir == RenderDirection.HORIZONTAL ? textureWidth / 2f : 0);
        float vOffset = (hovered && dir == RenderDirection.VERTICAL ? textureHeight / 2f : 0);

        drawTexturedRect(
                guiGraphics,
                texture,
                CustomColor.NONE,
                x,
                y,
                renderWidth,
                renderHeight,
                uOffset,
                vOffset,
                renderWidth,
                renderHeight,
                textureWidth,
                textureHeight);
    }

    public static void drawScalingHoverableTexturedRect(
            GuiGraphics guiGraphics,
            Texture texture,
            float x,
            float y,
            float width,
            float height,
            boolean hovered,
            RenderDirection dir) {
        int textureWidth = texture.width();
        int textureHeight = texture.height();

        int regionWidth = (dir == RenderDirection.HORIZONTAL ? textureWidth / 2 : textureWidth);
        int regionHeight = (dir == RenderDirection.VERTICAL ? textureHeight / 2 : textureHeight);

        float uOffset = (hovered && dir == RenderDirection.HORIZONTAL ? regionWidth : 0);
        float vOffset = (hovered && dir == RenderDirection.VERTICAL ? regionHeight : 0);

        drawTexturedRect(
                guiGraphics,
                texture,
                CustomColor.NONE,
                x,
                y,
                width,
                height,
                uOffset,
                vOffset,
                regionWidth,
                regionHeight,
                textureWidth,
                textureHeight);
    }

    public static void renderVignetteOverlay(GuiGraphics guiGraphics, CustomColor color, float alpha) {
        Window window = McUtils.window();

        drawTexturedRect(
                guiGraphics,
                Texture.VIGNETTE,
                color.withAlpha(alpha),
                0f,
                0f,
                window.getGuiScaledWidth(),
                window.getGuiScaledHeight(),
                0,
                0,
                Texture.VIGNETTE.width(),
                Texture.VIGNETTE.height(),
                Texture.VIGNETTE.width(),
                Texture.VIGNETTE.height());
    }

    public static void fillGradient(
            GuiGraphics guiGraphics,
            float x1,
            float y1,
            float x2,
            float y2,
            CustomColor colorA,
            CustomColor colorB,
            RenderDirection direction) {
        guiGraphics.guiRenderState.submitGuiElement(new CustomRectangleRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(guiGraphics.pose()),
                x1,
                y1,
                x2,
                y2,
                colorA,
                colorB,
                direction,
                guiGraphics.scissorStack.peek()));
    }

    public static void drawArc(
            GuiGraphics guiGraphics,
            CustomColor color,
            float x,
            float y,
            float fill,
            int innerRadius,
            int outerRadius) {
        drawArc(guiGraphics, color, x, y, fill, innerRadius, outerRadius, 0);
    }

    public static void drawArc(
            GuiGraphics guiGraphics,
            CustomColor color,
            float x,
            float y,
            float fill,
            int innerRadius,
            int outerRadius,
            float angleOffset) {
        guiGraphics.guiRenderState.submitGuiElement(new ArcRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(guiGraphics.pose()),
                x,
                y,
                fill,
                innerRadius,
                outerRadius,
                angleOffset,
                color,
                guiGraphics.scissorStack.peek()));
    }

    public static void drawRoundedRectWithBorder(
            GuiGraphics guiGraphics,
            CustomColor borderColor,
            CustomColor fillColor,
            float x,
            float y,
            float width,
            float height,
            float lineWidth,
            int innerRadius,
            int outerRadius) {
        float x2 = x + width;
        float y2 = y + height;

        // Fill the rect
        final int fillOffset = (int) lineWidth;
        drawRect(
                guiGraphics,
                fillColor,
                x + fillOffset,
                y + fillOffset,
                width - fillOffset * 2,
                height - fillOffset * 2);
        drawLine(guiGraphics, fillColor, x + fillOffset, y + fillOffset, x2 - fillOffset, y + fillOffset, lineWidth);
        drawLine(guiGraphics, fillColor, x2 - fillOffset, y + fillOffset, x2 - fillOffset, y2 - fillOffset, lineWidth);
        drawLine(guiGraphics, fillColor, x + fillOffset, y2 - fillOffset, x2 - fillOffset, y2 - fillOffset, lineWidth);
        drawLine(guiGraphics, fillColor, x + fillOffset, y + fillOffset, x + fillOffset, y2 - fillOffset, lineWidth);

        float offset = outerRadius - 1;

        // Edges
        drawLine(guiGraphics, borderColor, x + offset, y, x2 - offset, y, lineWidth);
        drawLine(guiGraphics, borderColor, x2, y + offset, x2, y2 - offset, lineWidth);
        drawLine(guiGraphics, borderColor, x + offset, y2, x2 - offset, y2, lineWidth);
        drawLine(guiGraphics, borderColor, x, y + offset, x, y2 - offset, lineWidth);

        // Corners
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(-1, -1);
        drawRoundedCorner(guiGraphics, borderColor, x, y, innerRadius, outerRadius, Mth.HALF_PI * 3);
        drawRoundedCorner(guiGraphics, borderColor, x, y2 - offset * 2, innerRadius, outerRadius, (float) Math.PI);
        drawRoundedCorner(
                guiGraphics, borderColor, x2 - offset * 2, y2 - offset * 2, innerRadius, outerRadius, Mth.HALF_PI);
        drawRoundedCorner(guiGraphics, borderColor, x2 - offset * 2, y, innerRadius, outerRadius, 0);
        guiGraphics.pose().popMatrix();
    }

    private static void drawRoundedCorner(
            GuiGraphics guiGraphics,
            CustomColor color,
            float x,
            float y,
            int innerRadius,
            int outerRadius,
            float angleOffset) {
        drawArc(guiGraphics, color, x, y, 0.25f, innerRadius, outerRadius, angleOffset);
    }

    /**
     * drawProgressBar
     * Draws a progress bar (textureY1 and textureY2 now specify both textures with background being on top of the bar)
     *
     * @param guiGraphics guiGraphics to use
     * @param texture   the texture to use
     * @param customColor the color for the bar
     * @param x1        left x on screen
     * @param y1        top y on screen
     * @param x2        right x on screen
     * @param y2        bottom right on screen
     * @param textureX1 texture left x for the part
     * @param textureY1 texture top y for the part (top of background)
     * @param textureX2 texture right x for the part
     * @param textureY2 texture bottom y for the part (bottom of bar)
     * @param progress  progress of the bar, 0.0f to 1.0f is left to right and 0.0f to -1.0f is right to left
     */
    public static void drawColoredProgressBar(
            GuiGraphics guiGraphics,
            Texture texture,
            CustomColor customColor,
            float x1,
            float y1,
            float x2,
            float y2,
            int textureX1,
            int textureY1,
            int textureX2,
            int textureY2,
            float progress) {
        int half = (textureY1 + textureY2) / 2 + (textureY2 - textureY1) % 2;
        drawProgressBarBackground(guiGraphics, texture, x1, y1, x2, y2, textureX1, textureY1, textureX2, half);
        drawProgressBarForegroundWithColor(
                guiGraphics,
                texture,
                customColor,
                x1,
                y1,
                x2,
                y2,
                textureX1,
                half,
                textureX2,
                textureY2 + (textureY2 - textureY1) % 2,
                progress);
    }

    /**
     * drawProgressBar
     * Draws a progress bar (textureY1 and textureY2 now specify both textures with background being on top of the bar)
     *
     * @param guiGraphics guiGraphics to use
     * @param texture   the texture to use
     * @param x1        left x on screen
     * @param y1        top y on screen
     * @param x2        right x on screen
     * @param y2        bottom right on screen
     * @param textureX1 texture left x for the part
     * @param textureY1 texture top y for the part (top of background)
     * @param textureX2 texture right x for the part
     * @param textureY2 texture bottom y for the part (bottom of bar)
     * @param progress  progress of the bar, 0.0f to 1.0f is left to right and 0.0f to -1.0f is right to left
     */
    public static void drawProgressBar(
            GuiGraphics guiGraphics,
            Texture texture,
            float x1,
            float y1,
            float x2,
            float y2,
            int textureX1,
            int textureY1,
            int textureX2,
            int textureY2,
            float progress) {
        int half = (textureY1 + textureY2) / 2 + (textureY2 - textureY1) % 2;
        drawProgressBarBackground(guiGraphics, texture, x1, y1, x2, y2, textureX1, textureY1, textureX2, half);
        drawProgressBarForeground(
                guiGraphics,
                texture,
                x1,
                y1,
                x2,
                y2,
                textureX1,
                half,
                textureX2,
                textureY2 + (textureY2 - textureY1) % 2,
                progress);
    }

    public static void drawProgressBarForeground(
            GuiGraphics guiGraphics,
            Texture texture,
            float x1,
            float y1,
            float x2,
            float y2,
            int textureX1,
            int textureY1,
            int textureX2,
            int textureY2,
            float progress) {
        if (progress == 0f) {
            return;
        }

        float width = x2 - x1;
        float texWidth = textureX2 - textureX1;

        float pxMin = x1;
        float pxMax = x2;

        float uOffset = textureX1;
        float uSize = textureX2 - textureX1;

        if (progress < 1f && progress > -1f) {
            if (progress < 0f) {
                float cut = 1f + progress;
                pxMin = x1 + width * cut;

                uOffset = textureX1 + texWidth * cut;
                uSize = textureX2 - uOffset;
            } else {
                float cut = progress;
                pxMax = x1 + width * cut;

                uSize = texWidth * cut;
            }
        }

        drawTexturedRect(
                guiGraphics,
                CustomRenderPipelines.PROGRESS_BAR_PIPELINE,
                texture.identifier(),
                CommonColors.WHITE,
                pxMin,
                y1,
                pxMax - pxMin,
                y2 - y1,
                uOffset,
                textureY1,
                uSize,
                textureY2 - textureY1,
                texture.width(),
                texture.height());
    }

    private static void drawProgressBarForegroundWithColor(
            GuiGraphics guiGraphics,
            Texture texture,
            CustomColor customColor,
            float x1,
            float y1,
            float x2,
            float y2,
            int textureX1,
            int textureY1,
            int textureX2,
            int textureY2,
            float progress) {
        if (progress == 0f) {
            return;
        }

        float width = x2 - x1;
        float texWidth = textureX2 - textureX1;

        float pxMin = x1;
        float pxMax = x2;

        float uOffset = textureX1;
        float uSize = textureX2 - textureX1;

        if (progress < 1f && progress > -1f) {
            if (progress < 0f) {
                float cut = 1f + progress;
                pxMin = x1 + width * cut;

                uOffset = textureX1 + texWidth * cut;
                uSize = textureX2 - uOffset;
            } else {
                float cut = progress;
                pxMax = x1 + width * cut;

                uSize = texWidth * cut;
            }
        }

        drawTexturedRect(
                guiGraphics,
                CustomRenderPipelines.PROGRESS_BAR_PIPELINE,
                texture.identifier(),
                customColor,
                pxMin,
                y1,
                pxMax - pxMin,
                y2 - y1,
                uOffset,
                textureY1,
                uSize,
                textureY2 - textureY1,
                texture.width(),
                texture.height());
    }

    public static void drawProgressBarBackground(
            GuiGraphics guiGraphics,
            Texture texture,
            float x1,
            float y1,
            float x2,
            float y2,
            int textureX1,
            int textureY1,
            int textureX2,
            int textureY2) {
        drawTexturedRect(
                guiGraphics,
                CustomRenderPipelines.PROGRESS_BAR_PIPELINE,
                texture.identifier(),
                CommonColors.WHITE,
                x1,
                y1,
                x2 - x1,
                y2 - y1,
                textureX1,
                textureY1,
                textureX2 - textureX1,
                textureY2 - textureY1,
                texture.width(),
                texture.height());
    }

    public static void enableScissor(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.enableScissor(x, y, x + width, y + height);
    }

    public static void disableScissor(GuiGraphics guiGraphics) {
        if (guiGraphics.scissorStack.stack.isEmpty()) return;

        guiGraphics.disableScissor();
    }

    public static void rotatePose(Matrix3x2fStack matrix3x2fStack, float centerX, float centerZ, float angle) {
        matrix3x2fStack.translate(centerX, centerZ);
        matrix3x2fStack.rotate((float) Math.toRadians(angle));
        matrix3x2fStack.translate(-centerX, -centerZ);
    }

    public static void renderItem(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        guiGraphics.renderItem(itemStack, x, y);
    }

    public static void renderTooltip(
            GuiGraphics guiGraphics,
            Font font,
            List<Component> tooltipLines,
            Optional<TooltipComponent> tooltipImage,
            int mouseX,
            int mouseY) {
        List<ClientTooltipComponent> list = tooltipLines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .collect(Util.toMutableList());
        tooltipImage.ifPresent(
                tooltipComponent -> list.add(list.isEmpty() ? 0 : 1, ClientTooltipComponent.create(tooltipComponent)));
        guiGraphics.renderTooltip(font, list, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
    }

    public static void renderCustomNametag(
            PoseStack poseStack,
            Component nametag,
            float nametagScale,
            float customOffset,
            int backgroundColor,
            EntityRenderState entityRenderState,
            CameraRenderState cameraRenderState,
            SubmitNodeCollector collector) {
        poseStack.pushPose();
        poseStack.translate(
                entityRenderState.nameTagAttachment.x,
                entityRenderState.nameTagAttachment.y + 0.3F + customOffset,
                entityRenderState.nameTagAttachment.z);
        poseStack.mulPose(cameraRenderState.orientation);
        poseStack.scale(0.025F * nametagScale, -0.025F * nametagScale, 0.025F * nametagScale);
        Matrix4f matrix4f = new Matrix4f(poseStack.last().pose());
        float xOffset = -McUtils.mc().font.width(nametag) / 2.0F;

        SubmitNodeStorage nodeStorage = (SubmitNodeStorage) collector;
        SubmitNodeCollection nodeCollection = nodeStorage.order(0);
        NameTagFeatureRenderer.Storage rendererStorage = nodeCollection.getNameTagSubmits();

        if (!entityRenderState.isDiscrete) {
            rendererStorage.nameTagSubmitsNormal.add(new SubmitNodeStorage.NameTagSubmit(
                    matrix4f,
                    xOffset,
                    0,
                    nametag,
                    LightTexture.lightCoordsWithEmission(entityRenderState.lightCoords, 2),
                    -1,
                    0,
                    entityRenderState.distanceToCameraSq));
            rendererStorage.nameTagSubmitsSeethrough.add(new SubmitNodeStorage.NameTagSubmit(
                    matrix4f,
                    xOffset,
                    0,
                    nametag,
                    entityRenderState.lightCoords,
                    NAMETAG_COLOR,
                    backgroundColor,
                    entityRenderState.distanceToCameraSq));
        } else {
            rendererStorage.nameTagSubmitsNormal.add(new SubmitNodeStorage.NameTagSubmit(
                    matrix4f,
                    xOffset,
                    0,
                    nametag,
                    entityRenderState.lightCoords,
                    NAMETAG_COLOR,
                    backgroundColor,
                    entityRenderState.distanceToCameraSq));
        }

        poseStack.popPose();
    }

    public static void renderLeaderboardBadge(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            EntityRenderState entityState,
            CameraRenderState cameraState,
            Identifier texture,
            float width,
            float height,
            float uOffset,
            float vOffset,
            float u,
            float v,
            float textureWidth,
            float textureHeight,
            float customOffset,
            float horizontalShift,
            float verticalShift) {
        poseStack.pushPose();

        poseStack.translate(
                entityState.nameTagAttachment.x,
                entityState.nameTagAttachment.y + 0.35f + customOffset,
                entityState.nameTagAttachment.z);
        poseStack.mulPose(cameraState.orientation);
        poseStack.scale(0.025f, -0.025f, 0.025f);

        float halfWidth = width / 2f;
        float halfHeight = height / 2f;

        float u1 = uOffset / textureWidth;
        float v1 = vOffset / textureHeight;
        float u2 = (uOffset + u) / textureWidth;
        float v2 = (vOffset + v) / textureHeight;

        CustomColor badgeColor;
        if (entityState.isDiscrete) {
            badgeColor = CommonColors.WHITE.brightnessShift(-0.4f);
        } else {
            badgeColor = CommonColors.WHITE;
        }

        ((SubmitNodeStorage) collector)
                .order(0)
                .submitCustomGeometry(poseStack, RenderTypes.text(texture), (pose, vertexConsumer) -> {
                    vertexConsumer
                            .addVertex(pose, -halfWidth + horizontalShift, -halfHeight - verticalShift, 0)
                            .setUv(u1, v1)
                            .setLight(entityState.lightCoords)
                            .setColor(badgeColor.asInt());

                    vertexConsumer
                            .addVertex(pose, -halfWidth + horizontalShift, halfHeight - verticalShift, 0)
                            .setUv(u1, v2)
                            .setLight(entityState.lightCoords)
                            .setColor(badgeColor.asInt());

                    vertexConsumer
                            .addVertex(pose, halfWidth + horizontalShift, halfHeight - verticalShift, 0)
                            .setUv(u2, v2)
                            .setLight(entityState.lightCoords)
                            .setColor(badgeColor.asInt());

                    vertexConsumer
                            .addVertex(pose, halfWidth + horizontalShift, -halfHeight - verticalShift, 0)
                            .setUv(u2, v1)
                            .setLight(entityState.lightCoords)
                            .setColor(badgeColor.asInt());
                });

        poseStack.popPose();
    }

    public static void drawMulticoloredRectBorders(
            GuiGraphics guiGraphics,
            List<CustomColor> colors,
            float x,
            float y,
            float width,
            float height,
            float externalLineWidth,
            float internalLineWidth) {
        if (colors.size() == 1) {
            drawRectBorders(guiGraphics, colors.getFirst(), x, y, x + width, y + height, externalLineWidth);
            return;
        }
        float splitX = width / (colors.size() - 1);

        for (int i = 0; i < colors.size(); i++) {
            CustomColor color = colors.get(i);
            float leftX = Mth.clamp(x + splitX * (i - 1), x, x + width);
            float centerX = Mth.clamp(x + splitX * i, x, x + width);
            float rightX = Mth.clamp(x + splitX * (i + 1), x, x + width);

            // bottom left to bottom center (always drawn)
            drawLine(guiGraphics, color, leftX, y + height, centerX, y + height, externalLineWidth);
            // bottom center to top right (drawn on i!=colors.size()-1)
            drawLine(
                    guiGraphics,
                    color,
                    centerX,
                    y + height,
                    rightX,
                    y,
                    (i == colors.size() - 1 ? externalLineWidth : internalLineWidth));
            // top right to top center (always drawn)
            drawLine(guiGraphics, color, rightX, y, centerX, y, externalLineWidth);
            // top center to bottom left (drawn on i!=0)
            drawLine(
                    guiGraphics,
                    color,
                    centerX,
                    y,
                    leftX,
                    y + height,
                    (i != 0 ? internalLineWidth : externalLineWidth));
        }
    }

    public static void drawMulticoloredRect(
            GuiGraphics guiGraphics, List<CustomColor> colors, float x, float y, float width, float height) {
        if (colors.size() == 1) {
            drawRect(guiGraphics, colors.getFirst(), x, y, width, height);
            return;
        }

        guiGraphics.guiRenderState.submitGuiElement(new MulticoloredRectangleRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(guiGraphics.pose()),
                x,
                y,
                x + width,
                y + height,
                width,
                colors,
                guiGraphics.scissorStack.peek()));
    }

    public static void createMask(GuiGraphics guiGraphics, Texture texture, int x1, int y1, int x2, int y2) {
        createMask(guiGraphics, texture, x1, y1, x2, y2, 0, 0, texture.width(), texture.height());
    }

    /**
     * Creates a mask that will remove anything drawn after
     * this and before the next {clearMask()}(or {endGL()})
     * and is not inside the mask.
     * A mask, is a clear and white texture where anything
     * white will allow drawing.
     *
     * @param texture mask texture(please use Textures.Masks)
     * @param x1 bottom-left x(on screen)
     * @param y1 bottom-left y(on screen)
     * @param x2 top-right x(on screen)
     * @param y2 top-right y(on screen)
     */
    public static void createMask(
            GuiGraphics guiGraphics,
            Texture texture,
            float x1,
            float y1,
            float x2,
            float y2,
            int tx1,
            int ty1,
            int tx2,
            int ty2) {
        // See https://gist.github.com/burgerguy/8233170683ad93eea6aa27ee02a5c4d1

        GL11.glEnable(GL11.GL_STENCIL_TEST);

        // Enable writing to stencil
        //        RenderSystem.stencilMask(0xff);
        //        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT);
        //        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        //        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        // Disable writing to color or depth
        //        RenderSystem.colorMask(false, false, false, false);
        //        RenderSystem.depthMask(false);

        // Draw textured image
        int width = texture.width();
        int height = texture.height();
        drawTexturedRect(
                guiGraphics,
                texture.identifier(),
                x1,
                y1,
                x2 - x1,
                y2 - y1,
                tx1,
                ty1,
                tx2 - tx1,
                ty2 - ty1,
                width,
                height);

        // Reenable color and depth
        //        RenderSystem.colorMask(true, true, true, true);
        //        RenderSystem.depthMask(true);

        // Only write to stencil area
        //        RenderSystem.stencilMask(0x00);
        //        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        //        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xff);
    }

    /**
     * Clears the active rendering mask from the screen.
     * Based on Figura <a href="https://github.com/Kingdom-of-The-Moon/FiguraRewriteRewrite"> code</a>.
     */
    public static void clearMask() {
        //        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT);

        // Turn off writing to stencil buffer.
        //        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        //        RenderSystem.stencilMask(0x00);

        // Always succeed in the stencil test, no matter what.
        //        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
    }

    public static void renderDebugGrid(
            GuiGraphics guiGraphics, float gridDivisions, float dividedWidth, float dividedHeight) {
        for (int i = 1; i <= gridDivisions - 1; i++) {
            double x = dividedWidth * i;
            double y = dividedHeight * i;
            drawRect(guiGraphics, CommonColors.GRAY, (int) x, 0, 1, (int) (dividedHeight * gridDivisions));
            drawRect(guiGraphics, CommonColors.GRAY, 0, (int) y, (int) (dividedWidth * gridDivisions), 1);
            if (i % 2 == 0) continue; // reduce clutter
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(String.valueOf(i)),
                            (float) x,
                            dividedHeight * (gridDivisions / 2),
                            CommonColors.RED,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(String.valueOf(i)),
                            dividedWidth * (gridDivisions / 2),
                            (float) y,
                            CommonColors.CYAN,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }
    }
}
