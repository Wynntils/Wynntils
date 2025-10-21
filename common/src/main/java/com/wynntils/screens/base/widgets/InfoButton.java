/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class InfoButton extends WynntilsButton {
    public InfoButton(int x, int y, Component tooltip) {
        super(x, y, 20, 20, Component.literal("?"));
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        // Do nothing
    }
}
