/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import java.util.Objects;

public class TextInputBoxSettingsWidget extends TextInputBoxWidget {

    protected final ConfigHolder configHolder;

    protected TextInputBoxSettingsWidget(ConfigHolder configHolder, TextboxScreen textboxScreen, int width) {
        super(0, 0, width, FontRenderer.getInstance().getFont().lineHeight + 8, null, textboxScreen);
        this.configHolder = configHolder;
        setTextBoxInput(configHolder.getValue().toString());
    }

    public TextInputBoxSettingsWidget(ConfigHolder configHolder, TextboxScreen textboxScreen) {
        this(configHolder, textboxScreen, 100);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();
        poseStack.translate(0f, (height - FontRenderer.getInstance().getFont().lineHeight + 8) / 2f - 5, 0f);
        super.renderWidget(poseStack, mouseX, mouseY, partialTick);
        poseStack.popPose();
    }

    protected void onUpdate(String text) {
        Object parsedValue = configHolder.tryParseStringValue(text);
        if (parsedValue != null) {
            if (!Objects.equals(parsedValue, configHolder.getValue())) configHolder.setValue(parsedValue);

            setRenderColor(CommonColors.GREEN);
        } else {
            setRenderColor(CommonColors.RED);
        }
    }
}
