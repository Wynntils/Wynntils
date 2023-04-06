/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.text.CodedString;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.client.resources.language.I18n;

public class BooleanConfigOptionElement extends ConfigOptionElement {
    private static final CustomColor BORDER_COLOR = CommonColors.BLACK;
    private static final CustomColor FOREGROUND_COLOR = new CustomColor(98, 34, 8);
    private static final CustomColor HOVER_FOREGROUND_COLOR = new CustomColor(158, 52, 16);

    public BooleanConfigOptionElement(ConfigHolder configHolder) {
        super(configHolder);
    }

    @Override
    public void renderConfigAppropriateButton(
            PoseStack poseStack, float width, float height, int mouseX, int mouseY, float partialTicks) {
        final float renderHeight = FontRenderer.getInstance().getFont().lineHeight + 8;
        final float renderWidth = 50f;

        float renderY = (height - renderHeight) / 2f;

        boolean isHovered = mouseX >= 0
                && mouseY >= 0
                && mouseX <= renderWidth
                && mouseY <= renderY + renderHeight
                && mouseY >= renderY;

        Boolean value = (Boolean) configHolder.getValue();
        if (value == null) {
            value = true;
        }

        RenderUtils.drawRoundedRectWithBorder(
                poseStack,
                BORDER_COLOR,
                isHovered ? HOVER_FOREGROUND_COLOR : FOREGROUND_COLOR,
                0,
                renderY,
                0,
                renderWidth,
                renderHeight,
                1,
                3,
                3);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        CodedString.fromString(
                                value
                                        ? I18n.get("screens.wynntils.settingsScreen.booleanConfig.enabled")
                                        : I18n.get("screens.wynntils.settingsScreen.booleanConfig.disabled")),
                        0,
                        renderWidth,
                        renderY + FontRenderer.getInstance().getFont().lineHeight / 2f,
                        0,
                        value ? CommonColors.GREEN : CommonColors.RED,
                        HorizontalAlignment.CENTER,
                        TextShadow.OUTLINE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Boolean value = (Boolean) configHolder.getValue();

        if (value == null) {
            configHolder.setValue(false);
        } else {
            configHolder.setValue(!value);
        }

        return true;
    }
}
