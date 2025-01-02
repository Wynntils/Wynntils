/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.gearviewer.widgets;

import com.google.common.collect.Lists;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.McUtils;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public abstract class PlayerInteractionButton extends WynntilsButton {
    private final List<Component> tooltipText;

    public PlayerInteractionButton(int x, int y, Component tooltipText, Component buttonText) {
        super(x, y, 20, 20, buttonText);
        this.tooltipText = List.of(tooltipText);
    }

    @Override
    public void onPress() {
        McUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        if (isHovered) {
            McUtils.mc()
                    .screen
                    .setTooltipForNextRenderPass(Lists.transform(tooltipText, Component::getVisualOrderText));
        }
    }
}
