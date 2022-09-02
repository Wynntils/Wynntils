/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.render;

import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;

public record TextRenderSetting(
        float maxWidth,
        CustomColor customColor,
        HorizontalAlignment horizontalAlignment,
        VerticalAlignment verticalAlignment,
        FontRenderer.TextShadow shadow) {

    public static final TextRenderSetting DEFAULT = new TextRenderSetting(
            0, CommonColors.WHITE, HorizontalAlignment.Left, VerticalAlignment.Top, FontRenderer.TextShadow.NORMAL);

    public TextRenderSetting withMaxWidth(float maxWidth) {
        return new TextRenderSetting(
                maxWidth, this.customColor, this.horizontalAlignment, this.verticalAlignment, this.shadow);
    }

    public TextRenderSetting withCustomColor(CustomColor color) {
        return new TextRenderSetting(
                this.maxWidth, customColor, this.horizontalAlignment, this.verticalAlignment, this.shadow);
    }

    public TextRenderSetting withHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        return new TextRenderSetting(
                this.maxWidth, this.customColor, horizontalAlignment, this.verticalAlignment, this.shadow);
    }

    public TextRenderSetting withVerticalAlignment(VerticalAlignment alignment) {
        return new TextRenderSetting(
                this.maxWidth, this.customColor, this.horizontalAlignment, verticalAlignment, this.shadow);
    }

    public TextRenderSetting withTextShadow(FontRenderer.TextShadow textShadow) {
        return new TextRenderSetting(
                this.maxWidth, this.customColor, this.horizontalAlignment, this.verticalAlignment, shadow);
    }
}
