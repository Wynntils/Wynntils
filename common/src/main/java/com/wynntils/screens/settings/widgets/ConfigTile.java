/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class ConfigTile extends AbstractContainerEventHandler {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final WynntilsBookSettingsScreen settingsScreen;
    private final ConfigHolder configHolder;

    private final GeneralSettingsButton resetButton;
    private AbstractWidget configOptionElement;

    public ConfigTile(
            int x, int y, int width, int height, WynntilsBookSettingsScreen settingsScreen, ConfigHolder configHolder) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.settingsScreen = settingsScreen;
        this.configHolder = configHolder;
        this.configOptionElement = getWidgetFromConfigHolder(configHolder);
        this.resetButton = new ResetButton(
                configHolder, () -> configOptionElement = getWidgetFromConfigHolder(configHolder), width - 37, 0);
    }

    public void renderWidgets(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderDisplayName(poseStack);

        RenderUtils.drawLine(
                poseStack,
                CommonColors.GRAY,
                this.x,
                this.y + this.height,
                this.x + this.width,
                this.y + this.height,
                0,
                1);

        poseStack.pushPose();
        final int renderX = getXOffset();
        final int renderY = getYOffset();
        poseStack.translate(renderX, renderY, 0);
        resetButton.render(poseStack, mouseX - renderX, mouseY - renderY, partialTick);
        configOptionElement.render(poseStack, mouseX - renderX, mouseY - renderY, partialTick);
        poseStack.popPose();
    }

    private void renderDisplayName(PoseStack poseStack) {
        StyledText displayName = settingsScreen.configOptionContains(configHolder)
                ? StyledText.fromString(ChatFormatting.UNDERLINE + configHolder.getDisplayName())
                : StyledText.fromString(configHolder.getDisplayName());
        poseStack.pushPose();
        poseStack.scale(0.8f, 0.8f, 0);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        displayName,
                        getXOffset() / 0.8f,
                        (this.y + 3) / 0.8f,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        poseStack.popPose();
    }

    private int getYOffset() {
        return this.y + 12;
    }

    private int getXOffset() {
        return this.x + 3;
    }

    private AbstractWidget getWidgetFromConfigHolder(ConfigHolder configOption) {
        if (configOption.getType().equals(Boolean.class)) {
            return new BooleanSettingsButton(configOption);
        } else if (configOption.isEnum()) {
            return new EnumSettingsButton<>(configOption);
        } else if (configOption.getType().equals(CustomColor.class)) {
            return new CustomColorSettingsButton(configOption, settingsScreen);
        } else {
            return new TextInputBoxSettingsWidget(configOption, settingsScreen);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(resetButton, configOptionElement);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX - getXOffset(), mouseY - getYOffset(), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX - getXOffset(), mouseY - getYOffset(), button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX - getXOffset(), mouseY - getYOffset(), button);
    }
}
