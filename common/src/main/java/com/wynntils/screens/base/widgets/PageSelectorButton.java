/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.screens.base.WynntilsPagedScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.RenderDirection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class PageSelectorButton extends WynntilsButton {
    private static final Identifier BOOK_TURN_PAGE_ID =
            Identifier.fromNamespaceAndPath("wynntils", "ui.book.turn-page");
    private static final SoundEvent BOOK_TURN_PAGE_SOUND = SoundEvent.createVariableRangeEvent(BOOK_TURN_PAGE_ID);

    private final boolean forward;
    private final WynntilsPagedScreen screen;

    public PageSelectorButton(int x, int y, int width, int height, boolean forward, WynntilsPagedScreen screen) {
        super(x, y, width, height, Component.literal("Page Selector Button"));
        this.forward = forward;
        this.screen = screen;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (!isValid()) return;

        McUtils.playSoundUI(BOOK_TURN_PAGE_SOUND);

        if (forward) {
            screen.setCurrentPage(screen.getCurrentPage() + 1);
        } else {
            screen.setCurrentPage(screen.getCurrentPage() - 1);
        }
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Texture arrowTexture = this.forward ? Texture.FORWARD_ARROW_OFFSET : Texture.BACKWARD_ARROW_OFFSET;

        RenderUtils.drawHoverableTexturedRect(
                guiGraphics, arrowTexture, getX(), getY(), isValid() && !isHovered, RenderDirection.HORIZONTAL);
    }

    private boolean isValid() {
        return forward ? screen.getCurrentPage() < screen.getMaxPage() : screen.getCurrentPage() > 0;
    }
}
