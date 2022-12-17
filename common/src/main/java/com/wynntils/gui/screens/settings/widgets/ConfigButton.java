/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.gui.screens.settings.elements.BooleanConfigOptionElement;
import com.wynntils.gui.screens.settings.elements.ConfigOptionElement;
import com.wynntils.gui.screens.settings.elements.CustomColorConfigOptionElement;
import com.wynntils.gui.screens.settings.elements.EnumConfigOptionElement;
import com.wynntils.gui.screens.settings.elements.TextConfigOptionElement;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.StringUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ConfigButton extends AbstractButton {
    private final WynntilsBookSettingsScreen settingsScreen;
    private final ConfigHolder configHolder;

    private final GeneralSettingsButton resetButton;
    private ConfigOptionElement configOptionElement;

    public ConfigButton(
            int x, int y, int width, int height, WynntilsBookSettingsScreen settingsScreen, ConfigHolder configHolder) {
        super(x, y, width, height, new TextComponent(configHolder.getJsonName()));
        this.settingsScreen = settingsScreen;
        this.configHolder = configHolder;
        this.resetButton = new GeneralSettingsButton(
                this.x + this.width - 40,
                this.y + 13,
                35,
                12,
                new TranslatableComponent("screens.wynntils.settingsScreen.reset.name"),
                () -> {
                    configHolder.reset();
                    this.configOptionElement = getWidgetFromConfigHolder(configHolder);
                },
                List.of(new TranslatableComponent("screens.wynntils.settingsScreen.reset.description")));
        this.configOptionElement = getWidgetFromConfigHolder(configHolder);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        resetButton.render(poseStack, mouseX, mouseY, partialTick);

        String displayName = configHolder.getDisplayName();

        if (settingsScreen.configOptionContains(configHolder)) {
            displayName = ChatFormatting.UNDERLINE + displayName;
        }

        poseStack.pushPose();
        poseStack.scale(0.8f, 0.8f, 0);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        displayName,
                        (this.x + 3) / 0.8f,
                        (this.y + 3) / 0.8f,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NONE);
        poseStack.popPose();

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
        final int renderX = this.x + 3;
        final int renderY = this.y + 12;
        poseStack.translate(renderX, renderY, 0);
        configOptionElement.renderConfigAppropriateButton(
                poseStack, this.width - 45, 30, mouseX - renderX, mouseY - renderY, partialTick);
        poseStack.popPose();

        if (!resetButton.isHoveredOrFocused() && isHovered) {
            String description = configHolder.getDescription();
            String[] parts = StringUtils.wrapTextBySize(description, 200);
            List<Component> components = Arrays.stream(parts)
                    .map(s -> (Component) new TextComponent(s))
                    .toList();

            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    0,
                    components,
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return resetButton.mouseClicked(mouseX, mouseY, button)
                || configOptionElement.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onPress() {
        // noop
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    private ConfigOptionElement getWidgetFromConfigHolder(ConfigHolder configOption) {
        if (configOption.getType().equals(Boolean.class)) {
            return new BooleanConfigOptionElement(configOption);
        } else if (configOption.getClassOfConfigField().isEnum()) {
            return new EnumConfigOptionElement(configOption);
        } else if (configOption.getType().equals(CustomColor.class)) {
            return new CustomColorConfigOptionElement(configOption, settingsScreen);
        } else {
            return new TextConfigOptionElement(configOption, settingsScreen);
        }
    }
}
