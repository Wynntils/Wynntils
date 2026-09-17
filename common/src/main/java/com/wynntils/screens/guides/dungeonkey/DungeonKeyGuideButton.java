/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.dungeonkey;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DungeonKeyGuideButton extends GuideButton {
    private final GuideDungeonKeyItemStack guideDungeonKeyItemStack;

    public DungeonKeyGuideButton(int x, int y, GuideDungeonKeyItemStack itemStack) {
        super(x, y, itemStack);

        this.guideDungeonKeyItemStack = itemStack;
    }

    @Override
    public void renderContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor color = getColor();
        renderBaseItem(guiGraphics, color);

        renderTextOverlay(
                guiGraphics,
                guideDungeonKeyItemStack.getDungeon().getInitials(),
                -1,
                0,
                4,
                color,
                HorizontalAlignment.LEFT);

        renderFavoriteIcon(guiGraphics);
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.fromChatFormatting(guideDungeonKeyItemStack.getHighlightColor());
    }
}
