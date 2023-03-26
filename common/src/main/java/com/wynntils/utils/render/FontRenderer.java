/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public final class FontRenderer {
    private static final FontRenderer INSTANCE = new FontRenderer();
    private final Font font;

    private static final int NEWLINE_OFFSET = 10;

    private FontRenderer() {
        this.font = ((MinecraftAccessor) McUtils.mc()).getFont();
    }

    public static FontRenderer getInstance() {
        return INSTANCE;
    }

    public Font getFont() {
        return font;
    }

    public void renderText(
            PoseStack poseStack,
            String text,
            float x,
            float y,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow,
            float textScale) {
        MultiBufferSource.BufferSource bufferSource =
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        BufferedFontRenderer.getInstance()
                .renderText(
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

        bufferSource.endBatch();
    }

    public void renderText(
            PoseStack poseStack,
            String text,
            float x,
            float y,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow) {
        renderText(poseStack, text, x, y, customColor, horizontalAlignment, verticalAlignment, shadow, 1f);
    }

    public void renderAlignedTextInBox(
            PoseStack poseStack,
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

    public void renderAlignedHighlightedTextInBox(
            PoseStack poseStack,
            String text,
            float x1,
            float x2,
            float y1,
            float y2,
            float maxWidth,
            CustomColor textColor,
            CustomColor backgroundColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment) {
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

        float cursorRenderY =
                switch (verticalAlignment) {
                    case Top -> renderY - 2;
                    case Middle -> renderY - (font.lineHeight / 2f) - 2;
                    case Bottom -> renderY - font.lineHeight + 2;
                };

        RenderUtils.drawRect(
                poseStack, backgroundColor, renderX, cursorRenderY, 0, font.width(text), font.lineHeight + 2);

        renderAlignedTextInBox(
                poseStack,
                text,
                x1,
                x2,
                y1,
                y2,
                maxWidth,
                textColor,
                horizontalAlignment,
                verticalAlignment,
                TextShadow.NONE,
                1f);
    }

    public void renderAlignedTextInBox(
            PoseStack poseStack,
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
            renderText(poseStack, text, x, y, customColor, horizontalAlignment, verticalAlignment, shadow, textScale);
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
                    part,
                    x,
                    y + (i * font.lineHeight),
                    customColor,
                    horizontalAlignment,
                    verticalAlignment,
                    shadow);
        }
    }

    public void renderText(
            PoseStack poseStack,
            String text,
            float x,
            float y,
            float maxWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow) {
        renderText(poseStack, text, x, y, maxWidth, customColor, horizontalAlignment, verticalAlignment, shadow, 1f);
    }

    public void renderText(PoseStack poseStack, float x, float y, TextRenderTask line) {
        renderText(
                poseStack,
                line.getText(),
                x,
                y,
                line.getSetting().maxWidth(),
                line.getSetting().customColor(),
                line.getSetting().horizontalAlignment(),
                line.getSetting().verticalAlignment(),
                line.getSetting().shadow());
    }

    public void renderTexts(PoseStack poseStack, float x, float y, List<TextRenderTask> lines) {
        float currentY = y;
        for (TextRenderTask line : lines) {
            renderText(poseStack, x, currentY, line);
            // If we ask Mojang code the line height of an empty line we get 0 back so replace with space
            currentY += calculateRenderHeight(
                    line.getText().isEmpty() ? " " : line.getText(),
                    line.getSetting().maxWidth());
        }
    }

    // TODO this is basically renderAlignedTextInBox but with tasks instead, make signatures the same and remove code
    // dup
    public void renderTextsWithAlignment(
            PoseStack poseStack,
            float x,
            float y,
            List<TextRenderTask> toRender,
            float width,
            float height,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment) {
        float renderX =
                switch (horizontalAlignment) {
                    case Left -> x;
                    case Center -> x + width / 2;
                    case Right -> x + width;
                };

        float renderY =
                switch (verticalAlignment) {
                    case Top -> y;
                    case Middle -> y + (height - calculateRenderHeight(toRender)) / 2;
                    case Bottom -> y + (height - calculateRenderHeight(toRender));
                };

        renderTexts(poseStack, renderX, renderY, toRender);
    }

    public void renderTextWithAlignment(
            PoseStack poseStack,
            float renderX,
            float renderY,
            TextRenderTask toRender,
            float width,
            float height,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment) {
        renderTextsWithAlignment(
                poseStack, renderX, renderY, List.of(toRender), width, height, horizontalAlignment, verticalAlignment);
    }

    public float calculateRenderHeight(List<TextRenderTask> toRender) {
        if (toRender.isEmpty()) return 0f;

        float height = 0;
        int totalLineCount = 0;

        for (TextRenderTask textRenderTask : toRender) {
            if (textRenderTask.getSetting().maxWidth() == 0) {
                height += font.lineHeight;
            } else {
                height += font.wordWrapHeight(textRenderTask.getText(), (int)
                        textRenderTask.getSetting().maxWidth());
            }
            totalLineCount++;
        }

        // Add additional height for different render tasks, but not for same render task split into multiple lines
        height += (totalLineCount - 1) * (NEWLINE_OFFSET - font.lineHeight);

        return height;
    }

    public float calculateRenderHeight(List<String> lines, float maxWidth) {
        int sum = 0;
        for (String line : lines) {
            sum += font.wordWrapHeight(line, (int) maxWidth);
        }
        return sum;
    }

    public float calculateRenderHeight(String line, float maxWidth) {
        return font.wordWrapHeight(line, maxWidth == 0 ? Integer.MAX_VALUE : (int) maxWidth);
    }
}
