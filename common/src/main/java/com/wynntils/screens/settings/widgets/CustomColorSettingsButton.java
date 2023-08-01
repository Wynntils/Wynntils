/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.components.events.ContainerEventHandler;

public class CustomColorSettingsButton extends TextInputBoxSettingsWidget {
    public CustomColorSettingsButton(
            int x, int y, ConfigHolder configHolder, ContainerEventHandler containerStateAccess) {
        super(x, y, configHolder, containerStateAccess, 80);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(poseStack, mouseX, mouseY, partialTick);
        CustomColor value = (CustomColor) configHolder.getValue();
        RenderUtils.drawRect(poseStack, value, getX() + width + 2, getY(), 0, height, height);
    }
}
