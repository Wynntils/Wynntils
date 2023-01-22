/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.gui.widgets.TextInputBoxWidget;
import com.wynntils.utils.CommonColors;
import java.util.Objects;

public class TextConfigOptionElement extends ConfigOptionElement {
    protected final TextInputBoxWidget textInputBoxWidget;
    private boolean lastParseSuccessful = false;

    protected final float renderHeight;

    protected TextConfigOptionElement(ConfigHolder configHolder, WynntilsBookSettingsScreen screen, int renderWidth) {
        super(configHolder);

        this.renderHeight = FontRenderer.getInstance().getFont().lineHeight + 8;
        this.textInputBoxWidget =
                new TextInputBoxWidget(0, 0, renderWidth, (int) this.renderHeight, this::onTextInputUpdate, screen);
        this.textInputBoxWidget.setTextBoxInput(configHolder.getValue().toString());
    }

    public TextConfigOptionElement(ConfigHolder configHolder, WynntilsBookSettingsScreen screen) {
        this(configHolder, screen, 100);
    }

    @Override
    public void renderConfigAppropriateButton(
            PoseStack poseStack, float width, float height, int mouseX, int mouseY, float partialTicks) {
        poseStack.pushPose();
        poseStack.translate(0f, (height - renderHeight) / 2f - 5, 0f);

        textInputBoxWidget.render(poseStack, mouseX, mouseY, partialTicks);

        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return textInputBoxWidget.mouseClicked(mouseX, mouseY, button);
    }

    private void onTextInputUpdate(String textInput) {
        Object parsedValue = configHolder.tryParseStringValue(textInput);

        if (parsedValue != null) {
            if (!Objects.equals(parsedValue, configHolder.getValue())) {
                configHolder.setValue(parsedValue);
            }

            lastParseSuccessful = true;
            textInputBoxWidget.setRenderColor(CommonColors.GREEN);
        } else {
            lastParseSuccessful = false;
            textInputBoxWidget.setRenderColor(CommonColors.RED);
        }
    }
}
