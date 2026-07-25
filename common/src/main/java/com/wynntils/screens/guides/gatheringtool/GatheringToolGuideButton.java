/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gatheringtool;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

public class GatheringToolGuideButton extends GuideButton {
    private final GuideGatheringToolItemStack toolItemStack;

    public GatheringToolGuideButton(int x, int y, GuideGatheringToolItemStack itemStack) {
        super(x, y, itemStack);

        this.toolItemStack = itemStack;

        // FIXME: This should be applied to the ItemMaterial when deserialized
        itemStack.set(
                DataComponents.TOOLTIP_STYLE,
                Identifier.withDefaultNamespace(
                        toolItemStack.getGatheringToolInfo().gearTier().getApiName()));
    }

    // FIXME: This should be painted by ItemHighlightFeature instead...
    @Override
    protected CustomColor getColor() {
        return CustomColor.fromChatFormatting(
                toolItemStack.getGatheringToolInfo().gearTier().getChatFormatting());
    }
}
