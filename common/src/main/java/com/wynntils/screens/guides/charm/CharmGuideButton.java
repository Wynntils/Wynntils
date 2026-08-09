/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.charm;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.client.gui.GuiGraphics;

public class CharmGuideButton extends GuideButton {
    private final GuideCharmItemStack charmItemStack;
    private boolean builtTooltip = false;

    public CharmGuideButton(int x, int y, GuideCharmItemStack itemStack) {
        super(x, y, itemStack);

        this.charmItemStack = itemStack;
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!builtTooltip) {
            charmItemStack.buildTooltip();
            builtTooltip = true;
        }

        itemStack.queueGuideTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.fromChatFormatting(
                charmItemStack.getCharmInfo().tier().getChatFormatting());
    }
}
