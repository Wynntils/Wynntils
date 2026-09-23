/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emerald;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class EmeraldGuideButton extends GuideButton {
    private final GuideEmeraldItemStack emeraldItemStack;

    public EmeraldGuideButton(int x, int y, GuideEmeraldItemStack itemStack) {
        super(x, y, itemStack);

        this.emeraldItemStack = itemStack;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor color = getColor();
        renderBaseItem(guiGraphics, color);

        if (emeraldItemStack.getTier() > 0) {
            renderTextOverlay(
                    guiGraphics,
                    String.valueOf(emeraldItemStack.getTier()),
                    4,
                    16,
                    10,
                    color,
                    HorizontalAlignment.CENTER);
        }

        renderFavoriteIcon(guiGraphics);
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.fromChatFormatting(ChatFormatting.GREEN);
    }
}
