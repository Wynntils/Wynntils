/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;

public record TextRenderSetting(
        float maxWidth,
        CustomColor customColor,
        HorizontalAlignment horizontalAlignment,
        VerticalAlignment verticalAlignment,
        TextShadow shadow) {
    public static final TextRenderSetting DEFAULT = new TextRenderSetting(
            0, CommonColors.WHITE, HorizontalAlignment.LEFT, VerticalAlignment.TOP, TextShadow.NORMAL);

    public static final TextRenderSetting CENTERED = new TextRenderSetting(
            0, CommonColors.WHITE, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE, TextShadow.NORMAL);

    public TextRenderSetting withMaxWidth(float maxWidth) {
        return new TextRenderSetting(
                maxWidth, this.customColor, this.horizontalAlignment, this.verticalAlignment, this.shadow);
    }

    public TextRenderSetting withCustomColor(CustomColor customColor) {
        return new TextRenderSetting(
                this.maxWidth, customColor, this.horizontalAlignment, this.verticalAlignment, this.shadow);
    }

    public TextRenderSetting withHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        return new TextRenderSetting(
                this.maxWidth, this.customColor, horizontalAlignment, this.verticalAlignment, this.shadow);
    }

    public TextRenderSetting withVerticalAlignment(VerticalAlignment verticalAlignment) {
        return new TextRenderSetting(
                this.maxWidth, this.customColor, this.horizontalAlignment, verticalAlignment, this.shadow);
    }

    public TextRenderSetting withAlignment(
            HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
        return new TextRenderSetting(
                this.maxWidth, this.customColor, horizontalAlignment, verticalAlignment, this.shadow);
    }

    public TextRenderSetting withTextShadow(TextShadow textShadow) {
        return new TextRenderSetting(
                this.maxWidth, this.customColor, this.horizontalAlignment, this.verticalAlignment, textShadow);
    }
}
