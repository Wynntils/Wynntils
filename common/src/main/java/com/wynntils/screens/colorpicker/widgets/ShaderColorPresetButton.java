/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.colorpicker.widgets;

import com.wynntils.core.text.fonts.wynnfonts.WynntilsColorSwatchFont;
import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.WynncraftShaderColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ShaderColorPresetButton extends PresetColorButton {
    private final WynncraftShaderColor shader;

    public ShaderColorPresetButton(int x, int y, WynncraftShaderColor shader, ColorPickerScreen screen) {
        super(x, y, 12, 12, shader.color, screen);

        this.shader = shader;
        setMessage(Component.literal(EnumUtils.toNiceString(shader)));
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        WynntilsColorSwatchFont.render(guiGraphics, getX(), getY(), getWidth(), getHeight(), shader.color);

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(getMessage(), mouseX, mouseY);
        }
    }
}
