/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.screens.settings.WynntilsSettingsScreen;
import com.wynntils.gui.screens.settings.widgets.FeatureSettingWidget;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;

public class CustomColorConfigOptionElement extends TextConfigOptionElement {
    public CustomColorConfigOptionElement(
            ConfigHolder configHolder,
            FeatureSettingWidget featureSettingWidget,
            WynntilsSettingsScreen settingsScreen) {
        super(configHolder, featureSettingWidget, settingsScreen);
    }

    @Override
    protected void renderSuccessState(PoseStack poseStack) {
        poseStack.pushPose();

        poseStack.translate(-getTextInputHeight() * 1.2f, 0, 0);

        if (!this.lastParseSuccessful) {
            // Render cross
            RenderUtils.drawLine(
                    poseStack, CommonColors.RED, 0, 0, getTextInputHeight(), getTextInputHeight(), 0, 1.2f);
            RenderUtils.drawLine(
                    poseStack, CommonColors.RED, getTextInputHeight(), 0, 0, getTextInputHeight(), 0, 1.2f);
        } else {
            Object configHolderValue = configHolder.getValue();
            assert configHolderValue instanceof CustomColor;

            RenderUtils.drawRect(
                    poseStack, (CustomColor) configHolderValue, 0, 0, 0, getTextInputHeight(), getTextInputHeight());
        }

        poseStack.popPose();
    }
}
