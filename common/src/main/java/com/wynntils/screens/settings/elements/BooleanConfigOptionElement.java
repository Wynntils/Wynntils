/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.screens.settings.widgets.FeatureSettingWidget;

public class BooleanConfigOptionElement extends ConfigOptionElement {
    public BooleanConfigOptionElement(ConfigHolder configHolder, FeatureSettingWidget featureSettingWidget) {
        super(configHolder, featureSettingWidget);
    }

    @Override
    protected void renderConfigAppropriateButton(
            PoseStack poseStack, float width, float height, int mouseX, int mouseY, float partialTicks) {
        float size = featureSettingWidget.getConfigOptionElementSize() * 1.5f;

        final Texture switchTexture = (boolean) configHolder.getValue() ? Texture.SWITCH_ON : Texture.SWITCH_OFF;

        RenderUtils.drawTexturedRect(
                poseStack,
                switchTexture.resource(),
                width - width / 10f - size / 2,
                height / 2f - size / 2f,
                0,
                size * 2f,
                size,
                0,
                0,
                switchTexture.width(),
                switchTexture.height(),
                switchTexture.width(),
                switchTexture.height());
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        Boolean value = (Boolean) configHolder.getValue();

        if (value == null) {
            configHolder.setValue(false);
        } else {
            configHolder.setValue(!value);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}
}
