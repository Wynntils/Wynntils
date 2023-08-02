/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public class ConfigTile extends WynntilsButton {
    private final WynntilsBookSettingsScreen settingsScreen;
    private final Config<?> config;

    private final GeneralSettingsButton resetButton;
    private AbstractWidget configOptionElement;

    public ConfigTile(
            int x, int y, int width, int height, WynntilsBookSettingsScreen settingsScreen, Config<?> config) {
        super(x, y, width, height, Component.literal(config.getJsonName()));
        this.settingsScreen = settingsScreen;
        this.config = config;
        this.configOptionElement = getWidgetFromConfig(config);
        this.resetButton = new ResetButton(
                config, () -> configOptionElement = getWidgetFromConfig(config), x + width - 40, getRenderY());
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        resetButton.render(poseStack, mouseX, mouseY, partialTick);

        renderDisplayName(poseStack);

        RenderUtils.drawLine(
                poseStack,
                CommonColors.GRAY,
                this.getX(),
                this.getY() + this.height,
                this.getX() + this.width,
                this.getY() + this.height,
                0,
                1);

        poseStack.pushPose();
        final int renderX = getRenderX();
        final int renderY = getRenderY();
        poseStack.translate(renderX, renderY, 0);
        configOptionElement.render(poseStack, mouseX - renderX, mouseY - renderY, partialTick);
        poseStack.popPose();
    }

    private void renderDisplayName(PoseStack poseStack) {
        StyledText displayName = settingsScreen.configOptionContains(config)
                ? StyledText.fromString(ChatFormatting.UNDERLINE + config.getDisplayName())
                : StyledText.fromString(config.getDisplayName());
        poseStack.pushPose();
        poseStack.scale(0.8f, 0.8f, 0);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        displayName,
                        getRenderX() / 0.8f,
                        (this.getY() + 3) / 0.8f,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double actualMouseX = mouseX - getRenderX();
        double actualMouseY = mouseY - getRenderY();

        return resetButton.mouseClicked(mouseX, mouseY, button)
                || configOptionElement.mouseClicked(actualMouseX, actualMouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        double actualMouseX = mouseX - getRenderX();
        double actualMouseY = mouseY - getRenderY();

        return configOptionElement.mouseDragged(actualMouseX, actualMouseY, button, deltaX, deltaY)
                || super.mouseDragged(actualMouseX, actualMouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double actualMouseX = mouseX - getRenderX();
        double actualMouseY = mouseY - getRenderY();

        return configOptionElement.mouseReleased(actualMouseX, actualMouseY, button)
                || super.mouseReleased(actualMouseX, actualMouseY, button);
    }

    @Override
    public void onPress() {
        // noop
    }

    private int getRenderY() {
        return this.getY() + 12;
    }

    private int getRenderX() {
        return this.getX() + 3;
    }

    private <E extends Enum<E>> AbstractWidget getWidgetFromConfig(Config<?> configOption) {
        if (configOption.getType().equals(Boolean.class)) {
            return new BooleanSettingsButton((Config<Boolean>) configOption);
        } else if (configOption.isEnum()) {
            return new EnumSettingsButton<>((Config<E>) configOption);
        } else if (configOption.getType().equals(CustomColor.class)) {
            return new CustomColorSettingsButton((Config<CustomColor>) configOption, settingsScreen);
        } else {
            return new TextInputBoxSettingsWidget<>(configOption, settingsScreen);
        }
    }
}
