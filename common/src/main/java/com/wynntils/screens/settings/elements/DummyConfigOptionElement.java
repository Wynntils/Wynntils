/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.screens.settings.widgets.FeatureSettingWidget;

public class DummyConfigOptionElement extends ConfigOptionElement {
    public DummyConfigOptionElement(ConfigHolder configHolder, FeatureSettingWidget featureSettingWidget) {
        super(configHolder, featureSettingWidget);
    }

    @Override
    protected void renderConfigAppropriateButton(PoseStack poseStack, float width, float height) {}

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {}
}
