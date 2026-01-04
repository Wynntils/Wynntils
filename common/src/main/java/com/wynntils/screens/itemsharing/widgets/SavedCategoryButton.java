/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemsharing.widgets;

import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.itemsharing.SavedItemsScreen;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.RenderDirection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
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
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics, buttonTexture, this.getX(), this.getY(), this.isHovered, RenderDirection.VERTICAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        savedItemsScreen.scrollCategories(next ? 1 : -1);
        return super.mouseClicked(event, isDoubleClick);
    }

    // Unused
    @Override
    public void onPress(InputWithModifiers input) {}
}
