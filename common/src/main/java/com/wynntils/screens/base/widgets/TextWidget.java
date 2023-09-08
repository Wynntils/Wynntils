/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextWidget extends WynntilsButton {
    public TextWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawString(McUtils.mc().font, getMessage(), this.getX(), this.getY(), CommonColors.WHITE.asInt());
    }

    @Override
    public void onPress() {}
}
