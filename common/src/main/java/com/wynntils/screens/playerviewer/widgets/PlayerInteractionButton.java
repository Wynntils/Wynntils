/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.playerviewer.widgets;

import com.google.common.collect.Lists;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public abstract class PlayerInteractionButton extends WynntilsButton {
    protected List<Component> tooltipText;
    protected Texture icon;

    protected PlayerInteractionButton(int x, int y, Component tooltipText, Texture icon) {
        super(x, y, 20, 20, Component.empty());
        this.tooltipText = List.of(tooltipText);
        this.icon = icon;
    }

    /** Only to be used with dynamically updating buttons. Call updateIcon after this. */
    protected PlayerInteractionButton(int x, int y) {
        super(x, y, 20, 20, Component.empty());
    }

    @Override
    public void onPress(InputWithModifiers input) {
        McUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        // +3 to center icon with 1px border in 16x16 button
        RenderUtils.drawTexturedRect(guiGraphics, icon, this.getX() + 3, this.getY() + 3);

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(
                    Lists.transform(tooltipText, Component::getVisualOrderText), mouseX, mouseY);
        }
    }
}
