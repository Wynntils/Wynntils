/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.playerviewer.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
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
    public void onPress() {
        McUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.enableDepthTest();
        // +3 to center icon with 1px border in 16x16 button
        RenderUtils.drawTexturedRect(
                guiGraphics.pose(),
                icon.resource(),
                this.getX() + 3,
                this.getY() + 3,
                2,
                14,
                14,
                0,
                0,
                14,
                14,
                icon.width(),
                icon.height());
        RenderSystem.disableDepthTest();

        if (isHovered) {
            McUtils.screen().setTooltipForNextRenderPass(Lists.transform(tooltipText, Component::getVisualOrderText));
        }
    }
}
