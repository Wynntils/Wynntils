/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.reflection.ConfigField;
import com.wynntils.core.config.ui.base.ConfigButtonWidget;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.ColorUtils;

public class ConfigBooleanWidget extends ConfigButtonWidget<Boolean> {
    private String text;
    private boolean enabled;

    private static final int buttonColor = ColorUtils.generateColor(134, 194, 50, 1);
    private static final int textColor = ColorUtils.generateColor(134, 194, 50, 1);

    public ConfigBooleanWidget(ConfigField<Boolean> field) {
        super(field, 100, 100);

        enabled = field.getFieldValue();
        updateText(enabled);
    }

    private void updateText(boolean enabled) {
        text = enabled ? "Enabled" : "Disabled";
    }

    @Override
    public void onPress() {
        enabled = !enabled;

        setValue(enabled);
        updateText(enabled);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        fill(poseStack, x, y, x + width, y + width, buttonColor);
        drawCenteredString(
                poseStack, McUtils.mc().font, text, x + width / 2, y + width / 2, textColor);
    }
}
