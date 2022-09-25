/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.Font;

public final class FontRenderer {
    private static final FontRenderer INSTANCE = new FontRenderer();
    private final Font font;

    private static final int NEWLINE_OFFSET = 10;
    private static final CustomColor SHADOW_COLOR = CommonColors.BLACK;

    private FontRenderer() {
        this.font = ((MinecraftAccessor) McUtils.mc()).getFont();
    }

    public static FontRenderer getInstance() {
        return INSTANCE;
    }

    public Font getFont() {
        return font;
    }

    public int renderText(
            PoseStack poseStack,
            String text,
            float x,
            float y,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow) {
        float renderX;
        float renderY;

        if (text == null) return 0;

        // TODO: Add rainbow color support

        renderX = switch (horizontalAlignment) {
            case Left -> x;
            case Center -> x - (font.width(text) / 2f);
            case Right -> x - font.width(text);};

        renderY = switch (verticalAlignment) {
            case Top -> y;
            case Middle -> y - (font.lineHeight / 2f);
            case Bottom -> y - font.lineHeight;};

        switch (shadow) {
            case OUTLINE:
                int shadowColor = SHADOW_COLOR.withAlpha(customColor.a).asInt();
                String strippedText = ComponentUtils.stripColorFormatting(text);

                // draw outline behind text
                font.draw(poseStack, strippedText, renderX + 1, renderY, shadowColor);
                font.draw(poseStack, strippedText, renderX - 1, renderY, shadowColor);
                font.draw(poseStack, strippedText, renderX, renderY + 1, shadowColor);
                font.draw(poseStack, strippedText, renderX, renderY - 1, shadowColor);

                return font.draw(poseStack, text, renderX, renderY, customColor.asInt());
            case NORMAL:
                return font.drawShadow(poseStack, text, renderX, renderY, customColor.asInt());
            default:
                return font.draw(poseStack, text, renderX, renderY, customColor.asInt());
        }
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
                textShadow);
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
                textShadow);
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
                textShadow);
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
        if (text == null) return;

        if (maxWidth == 0 || font.width(text) < maxWidth) {
            renderText(poseStack, text, x, y, customColor, horizontalAlignment, verticalAlignment, shadow);
            return;
        }

        List<String> parts = Arrays.stream(StringUtils.wrapTextBySize(text, (int) maxWidth))
                .filter(s -> !s.isBlank())
                .toList();
        String lastPart = "";
        for (int i = 0; i < parts.size(); i++) {
            // copy the format codes to this part as well
            String part = getLastPartCodes(lastPart) + parts.get(i);
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

    private String getLastPartCodes(String lastPart) {
        if (!lastPart.contains("§")) return "";

        String lastPartCodes = "";
        int index;
        while ((index = lastPart.lastIndexOf("§")) != -1) {
            if (index >= lastPart.length() - 1) {
                // trailing §, no format code, skip it
                lastPart = lastPart.substring(0, index);
                continue;
            }
            String thisCode = lastPart.substring(index, index + 2);
            if (thisCode.charAt(1) == 'r') {
                // it's a reset code, we can stop looking
                break;
            }
            // prepend to codes since we're going backwards
            lastPartCodes = thisCode + lastPartCodes;

            lastPart = lastPart.substring(0, index);
        }

        return lastPartCodes;
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
            currentY += calculateRenderHeight(List.of(line));
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
                int lines = 1;
                if (textRenderTask.getText().contains(" ")) {
                    lines = Arrays.stream(StringUtils.wrapTextBySize(textRenderTask.getText(), (int)
                                    textRenderTask.getSetting().maxWidth()))
                            .filter(s -> !s.isBlank())
                            .toList()
                            .size();
                }

                height += lines * font.lineHeight;
            }
            totalLineCount++;
        }

        // Add additional height for different render tasks, but not for same render task split into multiple lines
        height += (totalLineCount - 1) * (NEWLINE_OFFSET - font.lineHeight);

        return height;
    }

    public float calculateRenderHeight(List<String> lines, float maxWidth) {
        return calculateRenderHeight(lines.stream()
                .map(s -> new TextRenderTask(s, TextRenderSetting.DEFAULT.withMaxWidth(maxWidth)))
                .toList());
    }

    public enum TextShadow {
        NONE,
        NORMAL,
        OUTLINE
    }
}
