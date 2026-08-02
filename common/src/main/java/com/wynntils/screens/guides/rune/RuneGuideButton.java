/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.rune;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

public class RuneGuideButton extends GuideButton {
    private static final CustomColor COLOR = CustomColor.fromInt(0xDF1B64);
    private static final Identifier TOOLTIP_STYLE = Identifier.withDefaultNamespace("resource");

    public RuneGuideButton(int x, int y, GuideRuneItemStack itemStack) {
        super(x, y, itemStack);

        itemStack.set(DataComponents.TOOLTIP_STYLE, TOOLTIP_STYLE);
    }

    @Override
    protected CustomColor getColor() {
        return COLOR;
    }
}
