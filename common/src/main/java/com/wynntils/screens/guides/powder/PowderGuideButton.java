/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.powder;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class PowderGuideButton extends GuideButton {
    private final GuidePowderItemStack powderItemStack;

    public PowderGuideButton(int x, int y, GuidePowderItemStack itemStack) {
        super(x, y, itemStack);

        this.powderItemStack = itemStack;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor color = getColor();
        renderBaseItem(guiGraphics, color);

        renderTextOverlay(
                guiGraphics,
                MathUtils.toRoman(powderItemStack.getTier()),
                4,
                16,
                10,
                color,
                HorizontalAlignment.CENTER);

        renderFavoriteIcon(guiGraphics);
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return powderItemStack.getElement().getColor();
    }
}
