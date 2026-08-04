/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gear;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

public class GearGuideButton extends GuideButton {
    private final GuideGearItemStack gearItemStack;
    private boolean builtTooltip = false;

    public GearGuideButton(int x, int y, GuideGearItemStack itemStack) {
        super(x, y, itemStack);

        this.gearItemStack = itemStack;

        // FIXME: This should be applied to the ItemMaterial when deserialized
        itemStack.set(
                DataComponents.TOOLTIP_STYLE,
                Identifier.withDefaultNamespace(
                        gearItemStack.getGearInfo().tier().getApiName()));
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!builtTooltip) {
            gearItemStack.buildTooltip();
            builtTooltip = true;
        }

        itemStack.queueGuideTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.fromChatFormatting(gearItemStack.getGearInfo().tier().getChatFormatting());
    }
}
