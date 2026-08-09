/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ward;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

public class WardGuideButton extends GuideButton {
    private static final CustomColor COLOR = CustomColor.fromInt(0xdf1b64);
    private static final Identifier TOOLTIP_STYLE = Identifier.withDefaultNamespace("resource");

    public WardGuideButton(int x, int y, GuideWardItemStack itemStack) {
        super(x, y, itemStack);

        itemStack.set(DataComponents.TOOLTIP_STYLE, TOOLTIP_STYLE);
    }

    @Override
    protected CustomColor getColor() {
        return COLOR;
    }
}
