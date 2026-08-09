/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.tome;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.client.gui.GuiGraphics;

public class TomeGuideButton extends GuideButton {
    private final GuideTomeItemStack tomeItemStack;
    private boolean builtTooltip = false;

    public TomeGuideButton(int x, int y, GuideTomeItemStack itemStack) {
        super(x, y, itemStack);

        this.tomeItemStack = itemStack;
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!builtTooltip) {
            tomeItemStack.buildTooltip();
            builtTooltip = true;
        }

        itemStack.queueGuideTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.fromChatFormatting(tomeItemStack.getTomeInfo().tier().getChatFormatting());
    }
}
