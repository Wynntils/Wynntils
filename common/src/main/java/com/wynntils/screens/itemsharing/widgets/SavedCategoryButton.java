/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemsharing.widgets;

import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.itemsharing.SavedItemsScreen;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SavedCategoryButton extends WynntilsButton {
    private final boolean next;
    private final SavedItemsScreen savedItemsScreen;
    private final Texture buttonTexture;

    public SavedCategoryButton(int x, int y, SavedItemsScreen screen, boolean next) {
        super(x, y, 11, 10, Component.literal("Saved Items Button"));
        this.next = next;
        this.savedItemsScreen = screen;
        this.buttonTexture = next ? Texture.ITEM_RECORD_BUTTON_LEFT : Texture.ITEM_RECORD_BUTTON_RIGHT;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics.pose(), buttonTexture, this.getX(), this.getY(), this.isHovered);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        savedItemsScreen.scrollCategories(next ? 1 : -1);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // Unused
    @Override
    public void onPress() {}
}
