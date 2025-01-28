/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.gearviewer.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

public abstract class PlayerInteractionButton extends WynntilsButton {
    protected List<Component> tooltipText;
    protected Texture icon;

    public PlayerInteractionButton(int x, int y, Component tooltipText, Texture icon) {
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
        RenderUtils.drawTexturedRect(
                guiGraphics.pose(),
                icon.resource(),
                this.getX() + 2,
                this.getY() + 2,
                2,
                16,
                16,
                0,
                0,
                14,
                14,
                icon.width(),
                icon.height()
                );
        RenderSystem.disableDepthTest();

        if (isHovered) {
            McUtils.mc()
                    .screen
                    .setTooltipForNextRenderPass(Lists.transform(tooltipText, Component::getVisualOrderText));
        }
    }
}
