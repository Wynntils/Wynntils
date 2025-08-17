/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.buffered;

import com.wynntils.core.text.StyledText;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.IterationDecision;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public final class BufferedFontRenderer {
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
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            StyledText text,
            float x,
            float y,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow,
            float textScale,
            Font.DisplayMode displayMode) {
        float renderX;
        float renderY;

        if (text == null) return;

        // TODO: Add rainbow color support

        renderX = switch (horizontalAlignment) {
            case LEFT -> x;
            case CENTER -> x - (font.width(text.getComponent()) / 2f * textScale);
            case RIGHT -> x - font.width(text.getComponent()) * textScale;
        };

        renderY = switch (verticalAlignment) {
            case TOP -> y;
            case MIDDLE -> y - (font.lineHeight / 2f * textScale);
            case BOTTOM -> y - font.lineHeight * textScale;
        };

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(renderX, renderY);
        guiGraphics.pose().scale(textScale, textScale);

        switch (shadow) {
            case NONE -> guiGraphics.drawString(font, text.getComponent(), 0, 0, customColor.asInt(), false);
            case NORMAL -> guiGraphics.drawString(font, text.getComponent(), 0, 0, customColor.asInt(), true);
            case OUTLINE -> {
                int shadowColor = SHADOW_COLOR.withAlpha(customColor.a()).asInt();
                Component strippedComponent = text.iterate((part, changes) -> {
                            changes.remove(part);
                            changes.add(part.withStyle(partStyle -> partStyle.withColor(ChatFormatting.BLACK)));
                            return IterationDecision.CONTINUE;
                        })
                        .getComponent();

                guiGraphics.drawString(font, strippedComponent, -1, 0, shadowColor, false);
                guiGraphics.drawString(font, strippedComponent, 1, 0, shadowColor, false);
                guiGraphics.drawString(font, strippedComponent, 0, -1, shadowColor, false);
                guiGraphics.drawString(font, strippedComponent, 0, 1, shadowColor, false);
                guiGraphics.drawString(font, text.getComponent(), 0, 0, customColor.asInt(), false);
            }
        }

        guiGraphics.pose().popMatrix();
    }

    public void renderText(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            StyledText text,
            float x,
            float y,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow,
            float textScale) {
        renderText(
                guiGraphics,
                bufferSource,
                text,
                x,
                y,
                customColor,
                horizontalAlignment,
                verticalAlignment,
                shadow,
                textScale,
                Font.DisplayMode.SEE_THROUGH);
    }

    public void renderAlignedTextInBox(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            StyledText[] lines,
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
        int lineHeight = font.lineHeight;
        List<StyledText> adjustedLines = new ArrayList<>();
        for (StyledText line : lines) {
            if (maxWidth == 0 || font.width(line.getComponent()) < maxWidth / textScale) {
                adjustedLines.add(line);
            } else {
                List<FormattedText> parts =
                        font.getSplitter().splitLines(line.getComponent(), (int) (maxWidth / textScale), Style.EMPTY);
                StyledText lastPart = StyledText.EMPTY;
                for (FormattedText part : parts) {
                    Style lastStyle = ComponentUtils.getLastPartCodes(lastPart);
                    StyledText text = StyledText.fromComponent(
                                    Component.literal("").withStyle(lastStyle))
                            .append(StyledText.fromComponent(ComponentUtils.formattedTextToComponent(part)));
                    lastPart = text;
                    adjustedLines.add(text);
                }
            }
        }

        float calculatedTextHeight = (adjustedLines.size() - 1) * lineHeight * textScale;

        float renderX =
                switch (horizontalAlignment) {
                    case LEFT -> x1;
                    case CENTER -> (x1 + x2) / 2f;
                    case RIGHT -> x2;
                };
        float renderY =
                switch (verticalAlignment) {
                    case TOP -> y1;
                    case MIDDLE -> (y1 + y2) / 2f - calculatedTextHeight / 2f;
                    case BOTTOM -> y2 - calculatedTextHeight;
                };
        float lineOffset = 0;
        for (StyledText text : adjustedLines) {
            renderText(
                    guiGraphics,
                    bufferSource,
                    text,
                    renderX,
                    renderY + lineOffset,
                    customColor,
                    horizontalAlignment,
                    verticalAlignment,
                    textShadow,
                    textScale);
            lineOffset += lineHeight * textScale;
        }
    }

    public void renderAlignedTextInBox(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            StyledText text,
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
        renderAlignedTextInBox(
                guiGraphics,
                bufferSource,
                new StyledText[] {text},
                x1,
                x2,
                y1,
                y2,
                maxWidth,
                customColor,
                horizontalAlignment,
                verticalAlignment,
                textShadow,
                textScale);
    }

    public void renderAlignedTextInBox(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            StyledText text,
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
                guiGraphics,
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
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            StyledText text,
            float x1,
            float x2,
            float y,
            float maxWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            TextShadow textShadow) {
        renderAlignedTextInBox(
                guiGraphics,
                bufferSource,
                text,
                x1,
                x2,
                y,
                y,
                maxWidth,
                customColor,
                horizontalAlignment,
                VerticalAlignment.TOP,
                textShadow,
                1f);
    }

    public void renderAlignedTextInBox(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            StyledText text,
            float x,
            float y1,
            float y2,
            float maxWidth,
            CustomColor customColor,
            VerticalAlignment verticalAlignment,
            TextShadow textShadow) {
        renderAlignedTextInBox(
                guiGraphics,
                bufferSource,
                text,
                x,
                x,
                y1,
                y2,
                maxWidth,
                customColor,
                HorizontalAlignment.LEFT,
                verticalAlignment,
                textShadow,
                1f);
    }

    public void renderText(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            StyledText text,
            float x,
            float y,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow) {
        renderText(
                guiGraphics, bufferSource, text, x, y, customColor, horizontalAlignment, verticalAlignment, shadow, 1f);
    }

    private void renderText(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            StyledText text,
            float x,
            float y,
            float maxWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow,
            float textScale) {
        if (text == null) return;

        if (maxWidth == 0 || font.width(text.getComponent()) < maxWidth / textScale) {
            renderText(
                    guiGraphics,
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

        List<FormattedText> parts =
                font.getSplitter().splitLines(text.getComponent(), (int) (maxWidth / textScale), Style.EMPTY);

        StyledText lastPart = StyledText.EMPTY;
        for (int i = 0; i < parts.size(); i++) {
            // copy the format codes to this part as well
            Style lastStyle = ComponentUtils.getLastPartCodes(lastPart);

            StyledText part = StyledText.fromComponent(Component.literal("").withStyle(lastStyle))
                    .append(StyledText.fromComponent(ComponentUtils.formattedTextToComponent(parts.get(i))));
            lastPart = part;

            renderText(
                    guiGraphics,
                    bufferSource,
                    part,
                    x,
                    y + (i * font.lineHeight * textScale),
                    customColor,
                    horizontalAlignment,
                    verticalAlignment,
                    shadow,
                    textScale);
        }
    }

    public void renderTextsWithAlignment(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            float x,
            float y,
            List<TextRenderTask> toRender,
            float width,
            float height,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment) {
        renderTextsWithAlignment(
                guiGraphics, bufferSource, x, y, toRender, width, height, horizontalAlignment, verticalAlignment, 1f);
    }

    public void renderTextsWithAlignment(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            float x,
            float y,
            List<TextRenderTask> toRender,
            float width,
            float height,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            float textScale) {
        float renderX =
                switch (horizontalAlignment) {
                    case LEFT -> x;
                    case CENTER -> x + width / 2;
                    case RIGHT -> x + width;
                };

        float renderY =
                switch (verticalAlignment) {
                    case TOP -> y;
                    case MIDDLE -> y + (height - FontRenderer.getInstance().calculateRenderHeight(toRender)) / 2;
                    case BOTTOM -> y + (height - FontRenderer.getInstance().calculateRenderHeight(toRender));
                };

        renderTexts(guiGraphics, bufferSource, renderX, renderY, toRender, textScale);
    }

    public void renderTexts(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, float x, float y, List<TextRenderTask> lines) {
        renderTexts(guiGraphics, bufferSource, x, y, lines, 1f);
    }

    private void renderTexts(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            float x,
            float y,
            List<TextRenderTask> lines,
            float textScale) {
        float currentY = y;
        for (TextRenderTask line : lines) {
            renderText(guiGraphics, bufferSource, x, currentY, line, textScale);
            currentY += FontRenderer.getInstance()
                            .calculateRenderHeight(
                                    line.getText(), line.getSetting().maxWidth() / textScale)
                    * textScale;
        }
    }

    public void renderText(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, float x, float y, TextRenderTask line) {
        renderText(
                guiGraphics,
                bufferSource,
                line.getText(),
                x,
                y,
                line.getSetting().maxWidth(),
                line.getSetting().customColor(),
                line.getSetting().horizontalAlignment(),
                line.getSetting().verticalAlignment(),
                line.getSetting().shadow());
    }

    private void renderText(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            float x,
            float y,
            TextRenderTask line,
            float textScale) {
        renderText(
                guiGraphics,
                bufferSource,
                line.getText(),
                x,
                y,
                line.getSetting().maxWidth(),
                line.getSetting().customColor(),
                line.getSetting().horizontalAlignment(),
                line.getSetting().verticalAlignment(),
                line.getSetting().shadow(),
                textScale);
    }

    private void renderText(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            StyledText text,
            float x,
            float y,
            float maxWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow) {
        renderText(
                guiGraphics,
                bufferSource,
                text,
                x,
                y,
                maxWidth,
                customColor,
                horizontalAlignment,
                verticalAlignment,
                shadow,
                1f);
    }

    public void renderTextWithAlignment(
            GuiGraphics guiGraphics,
            MultiBufferSource bufferSource,
            float renderX,
            float renderY,
            TextRenderTask toRender,
            float width,
            float height,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment) {
        renderTextsWithAlignment(
                guiGraphics,
                bufferSource,
                renderX,
                renderY,
                List.of(toRender),
                width,
                height,
                horizontalAlignment,
                verticalAlignment);
    }
}
