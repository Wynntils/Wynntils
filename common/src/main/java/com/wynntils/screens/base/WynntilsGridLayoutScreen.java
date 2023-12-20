/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.wynntils.core.consumers.screens.WynntilsScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class WynntilsGridLayoutScreen extends WynntilsScreen {
    private static final float GRID_DIVISIONS = 64f;
    // Certain elements require static 20 height or button textures will break
    protected static final int BUTTON_HEIGHT = 20;
    protected float dividedHeight;
    protected float dividedWidth;
    /*
    Some notes on element alignment and sizing:
    All elements should be positioned dynamically relative to window size. That is; you must use this.height and
    this.width, plus any multiplier and optionally, +/- 1 to make small tweaks. For example, these are valid:
    (int) (dividedWidth * 36)
    (int) (dividedWidth * 57) - 1
    (this.width / 2)
    (end of another element) + x
    And these are invalid:
    100
    (int) (dividedWidth * 36) + 4
    (int) (this.width / 2 + 199)
    The reason for all of this is gui scale. Height and width will be different depending on the gui scale, and
    the above method will ensure that the gui is always aligned somewhat correctly.

    Also note that button height (and related elements), must be 20 or the texture will break.
    Element sizing isn't nearly as picky as alignment, but it'll still be better to keep static modifiers (eg. +2) to a
    minimum.
     */

    protected WynntilsGridLayoutScreen(Component component) {
        super(component);
    }

    @Override
    protected void doInit() {
        dividedHeight = this.height / GRID_DIVISIONS;
        dividedWidth = this.width / GRID_DIVISIONS;
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        //        RenderUtils.renderDebugGrid(guiGraphics.pose(), GRID_DIVISIONS, dividedWidth, dividedHeight);
    }
}
