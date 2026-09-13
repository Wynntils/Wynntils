/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class ComponentTypeButton extends WynntilsButton {
    private final String componentType;

    public ComponentTypeButton(int x, int y, int width, int height, String componentType) {
        super(x, y, width, height, Component.literal(componentType));
        this.componentType = componentType;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(componentType),
                        this.getX(),
                        this.getX() + this.width,
                        this.getY(),
                        0,
                        CommonColors.CYAN,
                        HorizontalAlignment.LEFT,
                        TextShadow.NORMAL);
    }

    @Override
    public void onPress(InputWithModifiers input) {}
}
