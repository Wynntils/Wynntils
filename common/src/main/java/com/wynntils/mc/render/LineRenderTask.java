/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.render;

import com.wynntils.utils.objects.CustomColor;

public record LineRenderTask(
        String text,
        float maxWidth,
        CustomColor customColor,
        FontRenderer.TextAlignment alignment,
        FontRenderer.TextShadow shadow) {

    public LineRenderTask(String text, float maxWidth, CustomColor customColor) {
        this(text, maxWidth, customColor, FontRenderer.TextAlignment.LEFT_ALIGNED, FontRenderer.TextShadow.NORMAL);
    }

    public static LineRenderTask getWithHorizontalAlignment(
            String text, float maxWidth, CustomColor customColor, HorizontalAlignment horizontalAlignment) {
        switch (horizontalAlignment) {
            case Left -> {
                return new LineRenderTask(
                        text,
                        maxWidth,
                        customColor,
                        FontRenderer.TextAlignment.LEFT_ALIGNED,
                        FontRenderer.TextShadow.NORMAL);
            }
            case Center -> {
                return new LineRenderTask(
                        text,
                        maxWidth,
                        customColor,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.NORMAL);
            }
            case Right -> {
                return new LineRenderTask(
                        text,
                        maxWidth,
                        customColor,
                        FontRenderer.TextAlignment.RIGHT_ALIGNED,
                        FontRenderer.TextShadow.NORMAL);
            }
        }

        return new LineRenderTask(
                text, maxWidth, customColor, FontRenderer.TextAlignment.LEFT_ALIGNED, FontRenderer.TextShadow.NORMAL);
    }
}
