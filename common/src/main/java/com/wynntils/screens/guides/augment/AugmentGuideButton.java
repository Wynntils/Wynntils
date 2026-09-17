/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.augment;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AugmentGuideButton extends GuideButton {
    private static final CustomColor TIER_COLOR = new CustomColor(0, 255, 255);

    private final GuideAugmentItemStack augmentItemStack;

    public AugmentGuideButton(int x, int y, GuideAugmentItemStack itemStack) {
        super(x, y, itemStack);

        this.augmentItemStack = itemStack;
    }

    @Override
    public void renderContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor color = getColor();
        renderBaseItem(guiGraphics, color);

        if (augmentItemStack.getTier() > 0) {
            renderTextOverlay(
                    guiGraphics,
                    MathUtils.toRoman(augmentItemStack.getTier()),
                    4,
                    16,
                    10,
                    TIER_COLOR,
                    HorizontalAlignment.CENTER);
        }

        renderFavoriteIcon(guiGraphics);
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.fromChatFormatting(augmentItemStack.getGearTier().getChatFormatting());
    }
}
