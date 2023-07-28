/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;

public class CustomColorSettingsButton extends TextInputBoxSettingsWidget {
    public CustomColorSettingsButton(ConfigHolder configHolder, TextboxScreen textboxScreen) {
        super(configHolder, textboxScreen, 80);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(poseStack, mouseX, mouseY, partialTick);
        CustomColor value = (CustomColor) configHolder.getValue();
        RenderUtils.drawRect(poseStack, value, width + 5, 6, 0, height, height);
    }
}
