/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.ColorPickerWidget;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.client.gui.GuiGraphics;

public class CustomColorSettingsButton extends TextInputBoxSettingsWidget<CustomColor> {
    private final ColorPickerWidget colorPickerWidget;

    public CustomColorSettingsButton(
            int x, int y, Config<CustomColor> config, TextboxScreen textboxScreen, int maskTopY, int maskBottomY) {
        super(x, y, config, textboxScreen, maskTopY, maskBottomY);

        colorPickerWidget = new ColorPickerWidget(getX() + getWidth() + 4, getY(), 20, 20, this);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        colorPickerWidget.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (colorPickerWidget.isMouseOver(mouseX, mouseY)) {
            return colorPickerWidget.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        colorPickerWidget.setY(y);
    }
}
