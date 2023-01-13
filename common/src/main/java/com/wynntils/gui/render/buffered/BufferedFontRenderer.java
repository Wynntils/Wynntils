/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.render.buffered;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class BufferedFontRenderer {
    private static final BufferedFontRenderer INSTANCE = new BufferedFontRenderer();
    private final Font font;

    private static final int NEWLINE_OFFSET = 10;
    private static final CustomColor SHADOW_COLOR = CommonColors.BLACK;

    private BufferedFontRenderer() {
        this.font = ((MinecraftAccessor) McUtils.mc()).getFont();
    }

    public static BufferedFontRenderer getInstance() {
        return INSTANCE;
    }

    public Font getFont() {
        return font;
    }

    public void renderText(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            String text,
            float x,
            float y,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow,
            float textScale) {
        float renderX;
        float renderY;

        if (text == null) return;

        // TODO: Add rainbow color support

        renderX = switch (horizontalAlignment) {
            case Left -> x;
            case Center -> x - (font.width(text) / 2f * textScale);
            case Right -> x - font.width(text) * textScale;};

        renderY = switch (verticalAlignment) {
            case Top -> y;
            case Middle -> y - (font.lineHeight / 2f * textScale);
            case Bottom -> y - font.lineHeight * textScale;};

        poseStack.pushPose();
        poseStack.translate(renderX, renderY, 0);
        poseStack.scale(textScale, textScale, 0);

        switch (shadow) {
            case NONE -> font.drawInBatch(
                    text,
                    0,
                    0,
                    customColor.asInt(),
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    true,
                    0,
                    0xF000F0,
                    font.isBidirectional());
            case NORMAL -> font.drawInBatch(
                    text,
                    0,
                    0,
                    customColor.asInt(),
                    true,
                    poseStack.last().pose(),
                    bufferSource,
                    true,
                    0,
                    0xF000F0,
                    font.isBidirectional());
            case OUTLINE -> {
                int shadowColor = SHADOW_COLOR.withAlpha(customColor.a).asInt();
                String strippedText = ComponentUtils.stripColorFormatting(text);

                font.drawInBatch(
                        strippedText,
                        -1,
                        0,
                        shadowColor,
                        false,
                        poseStack.last().pose(),
                        bufferSource,
                        false,
                        0,
                        0xF000F0,
                        font.isBidirectional());
                font.drawInBatch(
                        strippedText,
                        1,
                        0,
                        shadowColor,
                        false,
                        poseStack.last().pose(),
                        bufferSource,
                        false,
                        0,
                        0xF000F0,
                        font.isBidirectional());
                font.drawInBatch(
                        strippedText,
                        0,
                        -1,
                        shadowColor,
                        false,
                        poseStack.last().pose(),
                        bufferSource,
                        false,
                        0,
                        0xF000F0,
                        font.isBidirectional());
                font.drawInBatch(
                        strippedText,
                        0,
                        1,
                        shadowColor,
                        false,
                        poseStack.last().pose(),
                        bufferSource,
                        false,
                        0,
                        0xF000F0,
                        font.isBidirectional());

                font.drawInBatch(
                        text,
                        0,
                        0,
                        customColor.asInt(),
                        false,
                        poseStack.last().pose(),
                        bufferSource,
                        true,
                        0,
                        0xF000F0,
                        font.isBidirectional());
            }
        }

        poseStack.popPose();
    }

    public void renderAlignedTextInBox(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            String text,
            float x1,
            float x2,
            float y1,
            float y2,
            float maxWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow textShadow,
            float textScale) {

        float renderX =
                switch (horizontalAlignment) {
                    case Left -> x1;
                    case Center -> (x1 + x2) / 2f;
                    case Right -> x2;
                };

        float renderY =
                switch (verticalAlignment) {
                    case Top -> y1;
                    case Middle -> (y1 + y2) / 2f;
                    case Bottom -> y2;
                };

        renderText(
                poseStack,
                bufferSource,
                text,
                renderX,
                renderY,
                maxWidth,
                customColor,
                horizontalAlignment,
                verticalAlignment,
                textShadow,
                textScale);
    }

    public void renderAlignedTextInBox(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            String text,
            float x1,
            float x2,
            float y1,
            float y2,
            float maxWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow textShadow) {
        renderAlignedTextInBox(
                poseStack,
                bufferSource,
                text,
                x1,
                x2,
                y1,
                y2,
                maxWidth,
                customColor,
                horizontalAlignment,
                verticalAlignment,
                textShadow,
                1f);
    }

    public void renderAlignedTextInBox(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            String text,
            float x1,
            float x2,
            float y,
            float maxWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            TextShadow textShadow) {
        renderAlignedTextInBox(
                poseStack,
                bufferSource,
                text,
                x1,
                x2,
                y,
                y,
                maxWidth,
                customColor,
                horizontalAlignment,
                VerticalAlignment.Top,
                textShadow,
                1f);
    }

    public void renderAlignedTextInBox(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            String text,
            float x,
            float y1,
            float y2,
            float maxWidth,
            CustomColor customColor,
            VerticalAlignment verticalAlignment,
            TextShadow textShadow) {
        renderAlignedTextInBox(
                poseStack,
                bufferSource,
                text,
                x,
                x,
                y1,
                y2,
                maxWidth,
                customColor,
                HorizontalAlignment.Left,
                verticalAlignment,
                textShadow,
                1f);
    }

    public void renderText(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            String text,
            float x,
            float y,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow) {
        renderText(
                poseStack, bufferSource, text, x, y, customColor, horizontalAlignment, verticalAlignment, shadow, 1f);
    }

    public void renderText(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            String text,
            float x,
            float y,
            float maxWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow,
            float textScale) {
        if (text == null) return;

        if (maxWidth == 0 || font.width(text) < maxWidth) {
            renderText(
                    poseStack,
                    bufferSource,
                    text,
                    x,
                    y,
                    customColor,
                    horizontalAlignment,
                    verticalAlignment,
                    shadow,
                    textScale);
            return;
        }

        List<FormattedText> parts = font.getSplitter().splitLines(text, (int) maxWidth, Style.EMPTY);

        String lastPart = "";
        for (int i = 0; i < parts.size(); i++) {
            // copy the format codes to this part as well
            String part =
                    ComponentUtils.getLastPartCodes(lastPart) + parts.get(i).getString();
            lastPart = part;
            renderText(
                    poseStack,
                    bufferSource,
                    part,
                    x,
                    y + (i * font.lineHeight),
                    customColor,
                    horizontalAlignment,
                    verticalAlignment,
                    shadow);
        }
    }
}
