/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class GuidesButton extends WynntilsButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);

    private final float offsetX;
    private final float offsetY;
    private final Screen guideScreen;

    public GuidesButton(int x, int y, int width, int height, Screen guideScreen, float offsetX, float offsetY) {
        super(x, y, width, height, Component.literal("Guides Button"));
        this.guideScreen = guideScreen;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor backgroundColor = this.isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR;
        RenderUtils.drawRect(guiGraphics, backgroundColor, this.getX(), this.getY(), this.width, this.height);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromComponent(guideScreen.getTitle()),
                        this.getX() + 2,
                        this.getY() + 1,
                        this.width - 3,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE,
                        1f);
    }

    public void onPress(InputWithModifiers input) {
        McUtils.setScreen(guideScreen);
    }
}
