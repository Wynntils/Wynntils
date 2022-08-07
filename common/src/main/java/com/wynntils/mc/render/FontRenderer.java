/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.utils.objects.CustomColor;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.Font;

public class FontRenderer {
    private static final FontRenderer INSTANCE = new FontRenderer();
    private final Font font;

    private static final int NEWLINE_OFFSET = 10;
    private static final CustomColor SHADOW_COLOR = CommonColors.BLACK;

    public FontRenderer() {
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
            TextAlignment alignment,
            TextShadow shadow) {
        if (text == null) return 0;

        // TODO: Add rainbow color support

        switch (alignment) {
            case CENTER_ALIGNED:
                return renderText(
                        poseStack,
                        text,
                        x - font.width(text) / 2.0f,
                        y,
                        customColor,
                        TextAlignment.LEFT_ALIGNED,
                        shadow);
            case RIGHT_ALIGNED:
                return renderText(
                        poseStack, text, x - font.width(text), y, customColor, TextAlignment.LEFT_ALIGNED, shadow);
            default:
                switch (shadow) {
                    case OUTLINE:
                        int shadowColor = SHADOW_COLOR.withAlpha(customColor.a).asInt();
                        String strippedText = ComponentUtils.stripFormatting(text);

                        // draw outline behind text
                        font.draw(poseStack, strippedText, x, y, shadowColor);
                        font.draw(poseStack, strippedText, x + 1, y, shadowColor);
                        font.draw(poseStack, strippedText, x - 1, y, shadowColor);
                        font.draw(poseStack, strippedText, x, y + 1, shadowColor);
                        font.draw(poseStack, strippedText, x, y - 1, shadowColor);

                        return font.draw(poseStack, text, x, y, customColor.asInt());
                    case NORMAL:
                        return font.drawShadow(poseStack, text, x, y, customColor.asInt());
                    default:
                        return font.draw(poseStack, text, x, y, customColor.asInt());
                }
        }
    }

    public void renderAlignedTextInBox(
            PoseStack poseStack,
            String text,
            float x1,
            float x2,
            float y,
            float maxWidth,
            CustomColor customColor,
            TextAlignment textAlignment,
            TextShadow textShadow) {
        switch (textAlignment) {
            case LEFT_ALIGNED -> renderText(poseStack, text, x1, y, maxWidth, customColor, textAlignment, textShadow);
            case CENTER_ALIGNED -> renderText(
                    poseStack,
                    text,
                    x1 + (x2 - x1 - font.width(text)) / 2,
                    y,
                    maxWidth,
                    customColor,
                    TextAlignment.LEFT_ALIGNED,
                    textShadow);
            case RIGHT_ALIGNED -> renderText(
                    poseStack,
                    text,
                    x2 - font.width(text),
                    y,
                    maxWidth,
                    customColor,
                    TextAlignment.LEFT_ALIGNED,
                    textShadow);
        }
    }

    public void renderText(
            PoseStack poseStack,
            String text,
            float x,
            float y,
            float maxWidth,
            CustomColor customColor,
            TextAlignment alignment,
            TextShadow shadow) {
        if (text == null) return;

        if (maxWidth == 0 || font.width(text) < maxWidth) {
            renderText(poseStack, text, x, y, customColor, alignment, shadow);
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
            renderText(poseStack, part, x, y + (i * font.lineHeight), customColor, alignment, shadow);
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
                line.getSetting().alignment(),
                line.getSetting().shadow());
    }

    public void renderTexts(PoseStack poseStack, float x, float y, List<TextRenderTask> lines) {
        for (TextRenderTask line : lines) {
            renderText(poseStack, x, y, line);
            y += calculateRenderHeight(List.of(line));
        }
    }

    public void renderTextsWithAlignment(
            PoseStack poseStack,
            float renderX,
            float renderY,
            List<TextRenderTask> toRender,
            float width,
            float height,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment) {
        switch (horizontalAlignment) {
            case Center -> renderX += width / 2 / McUtils.window().getGuiScale();
            case Right -> renderX += width / McUtils.window().getGuiScale();
        }

        if (verticalAlignment != VerticalAlignment.Top) {
            float renderHeight = calculateRenderHeight(toRender);

            switch (verticalAlignment) {
                case Middle -> renderY +=
                        (height - renderHeight) / 2 / McUtils.window().getGuiScale();
                case Bottom -> renderY +=
                        (height - renderHeight) / McUtils.window().getGuiScale();
            }
        }

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
                .map(s -> new TextRenderTask(
                        s,
                        new TextRenderSetting(
                                maxWidth, CustomColor.NONE, TextAlignment.LEFT_ALIGNED, TextShadow.NORMAL)))
                .toList());
    }

    public enum TextAlignment {
        LEFT_ALIGNED,
        CENTER_ALIGNED,
        RIGHT_ALIGNED;

        public static TextAlignment fromHorizontalAlignment(HorizontalAlignment alignment) {
            return switch (alignment) {
                case Left -> LEFT_ALIGNED;
                case Center -> CENTER_ALIGNED;
                case Right -> RIGHT_ALIGNED;
            };
        }
    }

    public enum TextShadow {
        NONE,
        NORMAL,
        OUTLINE
    }
}
