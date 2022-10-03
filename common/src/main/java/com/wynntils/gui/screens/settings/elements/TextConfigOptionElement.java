/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.screens.settings.WynntilsSettingsScreen;
import com.wynntils.gui.screens.settings.widgets.FeatureSettingWidget;
import com.wynntils.gui.widgets.TextInputBoxWidget;
import com.wynntils.mc.objects.CommonColors;
import java.util.Objects;

public class TextConfigOptionElement extends ConfigOptionElement {
    private final TextInputBoxWidget textInputBoxWidget;
    private final WynntilsSettingsScreen settingsScreen;

    protected boolean lastParseSuccessful = false;

    public TextConfigOptionElement(
            ConfigHolder configHolder,
            FeatureSettingWidget featureSettingWidget,
            WynntilsSettingsScreen settingsScreen) {
        super(configHolder, featureSettingWidget, settingsScreen);
        this.settingsScreen = settingsScreen;

        float defaultSize = getConfigOptionElementSize();
        this.textInputBoxWidget = new TextInputBoxWidget(
                0, 0, (int) (defaultSize * 6), getTextInputHeight(), this::onTextInputUpdate, this.settingsScreen);
        this.textInputBoxWidget.setTextBoxInput(configHolder.getValue().toString());
    }

    @Override
    protected void renderConfigAppropriateButton(
            PoseStack poseStack, float width, float height, int mouseX, int mouseY, float partialTicks) {
        poseStack.pushPose();

        float size = this.textInputBoxWidget.getWidth();
        poseStack.translate(
                width - width / 10f - size / 2f - getConfigOptionElementSize(),
                height / 2f - getTextInputHeight() / 2f,
                0);

        this.textInputBoxWidget.render(poseStack, mouseX, mouseY, partialTicks);

        renderSuccessState(poseStack);

        poseStack.popPose();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        this.textInputBoxWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}

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

    private void onTextInputUpdate(String textInput) {
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
