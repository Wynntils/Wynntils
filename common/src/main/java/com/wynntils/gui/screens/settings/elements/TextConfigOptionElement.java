/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.gui.widgets.TextInputBoxWidget;
import com.wynntils.mc.objects.CommonColors;
import java.util.Objects;

public class TextConfigOptionElement extends ConfigOptionElement {
    protected final TextInputBoxWidget textInputBoxWidget;
    protected boolean lastParseSuccessful = false;

    public TextConfigOptionElement(ConfigHolder configHolder, WynntilsBookSettingsScreen screen) {
        super(configHolder);

        this.textInputBoxWidget =
                new TextInputBoxWidget(0, 0, 100, getTextInputHeight(), this::onTextInputUpdate, screen);
        this.textInputBoxWidget.setTextBoxInput(configHolder.getValue().toString());
    }

    @Override
    protected void renderConfigAppropriateButton(
            PoseStack poseStack, float width, float height, int mouseX, int mouseY, float partialTicks) {
        poseStack.pushPose();

        float size = this.textInputBoxWidget.getWidth();
        poseStack.translate(width - width / 10f - size / 2f, height / 2f - getTextInputHeight() / 2f, 0);

        this.textInputBoxWidget.render(poseStack, mouseX, mouseY, partialTicks);

        renderSuccessState(poseStack);

        poseStack.popPose();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        this.textInputBoxWidget.mouseClicked(mouseX, mouseY, button);
    }

    protected void renderSuccessState(PoseStack poseStack) {
        RenderUtils.drawRect(
                poseStack,
                lastParseSuccessful ? CommonColors.GREEN : CommonColors.RED,
                -getTextInputHeight() * 1.2f,
                0,
                0,
                getTextInputHeight(),
                getTextInputHeight());
    }

    protected void onTextInputUpdate(String textInput) {
        Object parsedValue = configHolder.tryParseStringValue(textInput);

        if (parsedValue != null) {
            if (!Objects.equals(parsedValue, configHolder.getValue())) {
                configHolder.setValue(parsedValue);
            }

            lastParseSuccessful = true;
        } else {
            lastParseSuccessful = false;
        }
    }

    protected static int getTextInputHeight() {
        return FontRenderer.getInstance().getFont().lineHeight + 4;
    }
}
