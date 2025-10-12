/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.wynntils.core.text.StyledText;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.IterationDecision;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public final class FontRenderer {
    private static final FontRenderer INSTANCE = new FontRenderer();
    private final Font font;

    private static final CustomColor SHADOW_COLOR = CommonColors.BLACK;
    private static final double HALF_PI = 1.5707963267948966;
    private static final double TWO_PI = 6.283185307179586;
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
            GuiGraphics guiGraphics,
            StyledText text,
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
            StyledText text,
            float x,
            float y,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow) {
        renderText(guiGraphics, text, x, y, customColor, horizontalAlignment, verticalAlignment, shadow, 1f);
    }

    public void renderAlignedTextInBox(
            GuiGraphics guiGraphics,
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
                1f);
    }

    public void renderAlignedHighlightedTextInBox(
            GuiGraphics guiGraphics,
            StyledText text,
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
                    case LEFT -> x1;
                    case CENTER -> (x1 + x2) / 2f;
                    case RIGHT -> x2;
                };

        float renderY =
                switch (verticalAlignment) {
                    case TOP -> y1;
                    case MIDDLE -> (y1 + y2) / 2f;
                    case BOTTOM -> y2;
                };

        float cursorRenderY =
                switch (verticalAlignment) {
                    case TOP -> renderY - 2;
                    case MIDDLE -> renderY - (font.lineHeight / 2f) - 2;
                    case BOTTOM -> renderY - font.lineHeight + 2;
                };

        RenderUtils.drawRect(
                poseStack,
                backgroundColor,
                renderX,
                cursorRenderY,
                0,
                font.width(text.getComponent()),
                font.lineHeight + 2);

        renderAlignedTextInBox(
                guiGraphics,
                new StyledText[] {text},
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
            GuiGraphics guiGraphics,
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
                new StyledText[] {text},
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
                new StyledText[] {text},
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

        if (maxWidth == 0 || font.width(text.getComponent()) / textScale < maxWidth) {
            renderText(guiGraphics, text, x, y, customColor, horizontalAlignment, verticalAlignment, shadow, textScale);
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

    public void renderText(
            GuiGraphics guiGraphics,
            StyledText text,
            float x,
            float y,
            float maxWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow) {
        renderText(guiGraphics, text, x, y, maxWidth, customColor, horizontalAlignment, verticalAlignment, shadow, 1f);
    }

    public void renderScrollingText(
            GuiGraphics guiGraphics,
            StyledText styledText,
            float x,
            float y,
            float renderWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow,
            float textScale) {
        int textLength = (int) ((font.width(styledText.getComponent()) + 1) * textScale);

        if (textLength > renderWidth) {
            float maxScrollOffset =
                    switch (horizontalAlignment) {
                        case CENTER -> -(textLength / 2f) + (renderWidth / 2);
                        case RIGHT -> 0.0f;
                        default -> textLength - renderWidth;
                    };

            double currentTimeInSeconds = (double) Util.getMillis() / 1000.0;
            double scrollFactor = Math.max((double) maxScrollOffset * 0.5, 3.0);
            double scrollPosition =
                    Math.sin(HALF_PI * Math.cos(TWO_PI * currentTimeInSeconds / scrollFactor)) / 2.0 + 0.5;

            float startOffset =
                    switch (horizontalAlignment) {
                        case CENTER -> (textLength / 2f) - (renderWidth / 2);
                        case RIGHT -> renderWidth - textLength;
                        default -> 0.0f;
                    };

            double scrollOffset = Mth.lerp(scrollPosition, startOffset, maxScrollOffset);

            float scissorX =
                    switch (horizontalAlignment) {
                        case LEFT -> x;
                        case CENTER -> x - (renderWidth / 2);
                        case RIGHT -> x - renderWidth;
                    };

            float scissorY =
                    switch (verticalAlignment) {
                        case TOP -> y;
                        case MIDDLE -> y - (font.lineHeight / 2f) - 1;
                        case BOTTOM -> y - font.lineHeight - 1;
                    };

            RenderUtils.createRectMask(poseStack, (int) scissorX, (int) scissorY, (int) renderWidth, (int)
                    ((font.lineHeight + 1) * textScale)); // + 1 to account for letters that sit lower, eg y
            renderText(
                    guiGraphics,
                    styledText,
                    x - (int) scrollOffset,
                    y,
                    customColor,
                    horizontalAlignment,
                    verticalAlignment,
                    shadow,
                    textScale);
            RenderUtils.clearMask();
        } else {
            renderText(
                    guiGraphics,
                    styledText,
                    x,
                    y,
                    customColor,
                    horizontalAlignment,
                    verticalAlignment,
                    shadow,
                    textScale);
        }
    }

    public void renderScrollingText(
            GuiGraphics guiGraphics,
            StyledText styledText,
            float x,
            float y,
            float renderWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow shadow) {
        renderScrollingText(
                guiGraphics,
                styledText,
                x,
                y,
                renderWidth,
                customColor,
                horizontalAlignment,
                verticalAlignment,
                shadow,
                1);
    }

    public void renderScrollingAlignedTextInBox(
            GuiGraphics guiGraphics,
            StyledText text,
            float x1,
            float x2,
            float y1,
            float y2,
            float renderWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow textShadow,
            float textScale) {
        float renderX =
                switch (horizontalAlignment) {
                    case LEFT -> x1;
                    case CENTER -> (x1 + x2) / 2f;
                    case RIGHT -> x2;
                };

        float renderY =
                switch (verticalAlignment) {
                    case TOP -> y1;
                    case MIDDLE -> (y1 + y2) / 2f;
                    case BOTTOM -> y2;
                };

        renderScrollingText(
                guiGraphics,
                text,
                renderX,
                renderY,
                renderWidth,
                customColor,
                horizontalAlignment,
                verticalAlignment,
                textShadow,
                textScale);
    }

    public void renderScrollingAlignedTextInBox(
            GuiGraphics guiGraphics,
            StyledText text,
            float x1,
            float x2,
            float y1,
            float y2,
            float renderWidth,
            CustomColor customColor,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            TextShadow textShadow) {
        renderScrollingAlignedTextInBox(
                guiGraphics,
                text,
                x1,
                x2,
                y1,
                y2,
                renderWidth,
                customColor,
                horizontalAlignment,
                verticalAlignment,
                textShadow,
                1f);
    }

    public void renderText(GuiGraphics guiGraphics, float x, float y, TextRenderTask line) {
        renderText(
                guiGraphics,
                line.getText(),
                x,
                y,
                line.getSetting().maxWidth(),
                line.getSetting().customColor(),
                line.getSetting().horizontalAlignment(),
                line.getSetting().verticalAlignment(),
                line.getSetting().shadow());
    }

    private void renderTexts(GuiGraphics guiGraphics, float x, float y, List<TextRenderTask> lines, float textScale) {
        float currentY = y;
        for (TextRenderTask line : lines) {
            renderText(guiGraphics, x, currentY, line, textScale);
            currentY += FontRenderer.getInstance()
                            .calculateRenderHeight(
                                    line.getText(), line.getSetting().maxWidth() / textScale)
                    * textScale;
        }
    }

    private void renderText(GuiGraphics guiGraphics, float x, float y, TextRenderTask line, float textScale) {
        renderText(
                guiGraphics,
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

    public void renderTexts(GuiGraphics guiGraphics, float x, float y, List<TextRenderTask> lines) {
        renderTexts(guiGraphics, x, y, lines, 1f);
    }

    public void renderTextsWithAlignment(
            GuiGraphics guiGraphics,
            float x,
            float y,
            List<TextRenderTask> toRender,
            float width,
            float height,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment) {
        float renderX =
                switch (horizontalAlignment) {
                    case LEFT -> x;
                    case CENTER -> x + width / 2;
                    case RIGHT -> x + width;
                };

        float renderY =
                switch (verticalAlignment) {
                    case TOP -> y;
                    case MIDDLE -> y + (height - calculateRenderHeight(toRender)) / 2;
                    case BOTTOM -> y + (height - calculateRenderHeight(toRender));
                };

        renderTexts(guiGraphics, renderX, renderY, toRender);
    }

    public void renderTextsWithAlignment(
            GuiGraphics guiGraphics,
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

        renderTexts(guiGraphics, renderX, renderY, toRender, textScale);
    }

    public void renderTextWithAlignment(
            GuiGraphics guiGraphics,
            float renderX,
            float renderY,
            TextRenderTask toRender,
            float width,
            float height,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment) {
        renderTextsWithAlignment(
                guiGraphics,
                renderX,
                renderY,
                List.of(toRender),
                width,
                height,
                horizontalAlignment,
                verticalAlignment);
    }

    public float calculateRenderHeight(List<TextRenderTask> toRender) {
        if (toRender.isEmpty()) return 0f;

        float height = 0;
        int totalLineCount = 0;

        for (TextRenderTask textRenderTask : toRender) {
            if (textRenderTask.getSetting().maxWidth() == 0) {
                height += font.lineHeight;
            } else {
                height += calculateRenderHeight(
                        textRenderTask.getText(), textRenderTask.getSetting().maxWidth());
            }
            totalLineCount++;
        }

        // Add additional height for different render tasks, but not for same render task split into multiple lines
        height += (totalLineCount - 1) * (NEWLINE_OFFSET - font.lineHeight);

        return height;
    }

    public float calculateRenderHeight(List<StyledText> lines, float maxWidth) {
        return (float) lines.stream()
                .mapToDouble(line -> calculateRenderHeight(line, maxWidth))
                .sum();
    }

    public float calculateRenderHeight(String line, float maxWidth) {
        return calculateRenderHeight(StyledText.fromString(line), maxWidth);
    }

    public float calculateRenderHeight(StyledText line, float maxWidth) {
        // If we ask Mojang code the line height of an empty line we get 0 back so replace with space
        return font.wordWrapHeight(
                line.isEmpty() ? Component.literal(" ") : line.getComponent(),
                maxWidth == 0 ? Integer.MAX_VALUE : (int) maxWidth);
    }
}
