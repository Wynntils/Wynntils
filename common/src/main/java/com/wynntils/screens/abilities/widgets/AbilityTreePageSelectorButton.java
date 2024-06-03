/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.abilities.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.screens.abilities.CustomAbilityTreeScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class AbilityTreePageSelectorButton extends AbstractWidget {
    private final CustomAbilityTreeScreen screen;
    private final boolean upDirection;

    public AbilityTreePageSelectorButton(
            int x, int y, int width, int height, CustomAbilityTreeScreen screen, boolean up) {
        super(x, y, width, height, Component.literal(up ? "Page Up" : "Page Down"));
        this.screen = screen;
        this.upDirection = up;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (upDirection && screen.getCurrentScrollPercentage() == 0) return;
        if (!upDirection && screen.getCurrentScrollPercentage() == Models.AbilityTree.ABILITY_TREE_PAGES - 1) return;

        Texture texture = upDirection ? Texture.ABILITY_TREE_UP_ARROW : Texture.ABILITY_TREE_DOWN_ARROW;

        RenderUtils.drawTexturedRect(guiGraphics.pose(), texture, this.getX(), this.getY());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if we're at the top or bottom of the tree
        if (upDirection && screen.getCurrentScrollPercentage() == 0) return true;
        if (!upDirection && screen.getCurrentScrollPercentage() == Models.AbilityTree.ABILITY_TREE_PAGES - 1)
            return true;

        if (upDirection) {
            screen.setCurrentScrollPercentage(screen.getCurrentScrollPercentage() - 0.1f);
        } else {
            screen.setCurrentScrollPercentage(screen.getCurrentScrollPercentage() + 0.1f);
        }

        McUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
