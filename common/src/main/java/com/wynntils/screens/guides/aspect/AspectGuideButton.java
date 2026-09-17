/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.aspect;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AspectGuideButton extends GuideButton {
    private static final CustomColor TIER_1_HIGHLIGHT_COLOR = CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY);
    private static final CustomColor TIER_2_HIGHLIGHT_COLOR = new CustomColor(205, 127, 50);
    private static final CustomColor TIER_3_HIGHLIGHT_COLOR = new CustomColor(192, 192, 192);
    private static final CustomColor TIER_4_HIGHLIGHT_COLOR = new CustomColor(255, 215, 0);

    private final GuideAspectItemStack aspectItemStack;
    private boolean builtTooltip = false;

    private final CustomColor textColor;

    public AspectGuideButton(int x, int y, GuideAspectItemStack itemStack) {
        super(x, y, itemStack);

        this.aspectItemStack = itemStack;

        textColor = switch (itemStack.getTier()) {
            case 2 -> TIER_2_HIGHLIGHT_COLOR;
            case 3 -> TIER_3_HIGHLIGHT_COLOR;
            case 4 -> TIER_4_HIGHLIGHT_COLOR;
            default -> TIER_1_HIGHLIGHT_COLOR;
        };
    }

    @Override
    public void renderContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBaseItem(guiGraphics, getColor());

        renderTextOverlay(
                guiGraphics,
                MathUtils.toRoman(aspectItemStack.getTier()),
                4,
                16,
                10,
                textColor,
                HorizontalAlignment.CENTER);

        renderFavoriteIcon(guiGraphics);
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (!builtTooltip) {
            aspectItemStack.buildTooltip();
            builtTooltip = true;
        }

        itemStack.queueGuideTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.fromChatFormatting(
                aspectItemStack.getAspectInfo().gearTier().getChatFormatting());
    }
}
