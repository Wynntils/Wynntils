/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.CustomColor;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.Font;

public class FontRenderer {
    private static final FontRenderer INSTANCE = new FontRenderer();
    private final Font font;

    private static final int NEWLINE_OFFSET = 10;

    public FontRenderer() {
        this.font = ((MinecraftAccessor) McUtils.mc()).getFont();
    }

    public static FontRenderer getInstance() {
        return INSTANCE;
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
            default: {
                if (shadow == TextShadow.NORMAL) {
                    return font.drawShadow(poseStack, text, x, y, customColor.asInt());
                }
                return font.draw(poseStack, text, x, y, customColor.asInt());
            }
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

        List<String> parts = Arrays.stream(text.split(" ")).toList();

        int line = 0;
        int partBegin = 0;

        for (int i = parts.size() - 1; i >= 0 && partBegin < parts.size(); i--) {
            String shortened = String.join(" ", parts.subList(partBegin, i + 1));
            if (font.width(shortened) < maxWidth) {
                renderText(poseStack, shortened, x, y + (line * NEWLINE_OFFSET), customColor, alignment, shadow);
                line++;
                partBegin = i + 1;
                i = parts.size();
            }
        }
    }

    private void renderText(PoseStack poseStack, float x, float y, TextRenderTask line) {
        renderText(poseStack, line.text(), x, y, line.maxWidth(), line.customColor(), line.alignment(), line.shadow());
    }

    public void renderTexts(PoseStack poseStack, float x, float y, List<TextRenderTask> lines) {
        for (int i = 0; i < lines.size(); i++) {
            renderText(poseStack, x, y + (NEWLINE_OFFSET * i), lines.get(i));
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

        float renderHeight = calculateRenderHeight(toRender);

        switch (verticalAlignment) {
            case Middle -> renderY +=
                    (height - renderHeight) / 2 / McUtils.window().getGuiScale();
            case Bottom -> renderY += (height - renderHeight) / McUtils.window().getGuiScale();
        }

        renderTexts(poseStack, renderX, renderY, toRender);
    }

    private float calculateRenderHeight(List<TextRenderTask> toRender) {
        float height = 0;
        for (TextRenderTask textRenderTask : toRender) {
            if (textRenderTask.maxWidth() == 0) {
                height += font.lineHeight + NEWLINE_OFFSET;
            } else {
                List<String> parts =
                        Arrays.stream(textRenderTask.text().split(" ")).toList();

                int lines = 0;
                int partBegin = 0;

                for (int i = parts.size() - 1; i >= 0 && partBegin < parts.size() - 1; i--) {
                    String shortened = String.join(" ", parts.subList(partBegin, i + 1));
                    if (font.width(shortened) < textRenderTask.maxWidth()) {
                        lines++;
                        partBegin = i + 1;
                        i = parts.size();
                    }
                }

                height += lines * (font.lineHeight + NEWLINE_OFFSET);
            }
        }

        return (float) (height / 2 * McUtils.window().getGuiScale());
    }

    public enum TextAlignment {
        LEFT_ALIGNED,
        CENTER_ALIGNED,
        RIGHT_ALIGNED
    }

    public enum TextShadow {
        NONE,
        NORMAL
    }
}
