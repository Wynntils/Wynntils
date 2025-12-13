/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.wynntilsmenu.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.RenderDirection;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WynntilsMenuButton extends AbstractWidget {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);

    private final Texture buttonTexture;
    private final boolean dynamicTexture;
    private final Runnable clickAction;
    private final List<Component> tooltipList;

    public WynntilsMenuButton(
            int x,
            int y,
            int size,
            Texture buttonTexture,
            boolean dynamicTexture,
            Runnable clickAction,
            List<Component> tooltipList) {
        super(x, y, size, size, Component.empty());

        this.buttonTexture = buttonTexture;
        this.dynamicTexture = dynamicTexture;
        this.clickAction = clickAction;
        this.tooltipList = tooltipList;
    }

    public WynntilsMenuButton(
            int x,
            int y,
            int size,
            Texture buttonTexture,
            boolean dynamicTexture,
            Screen openedScreen,
            List<Component> tooltipList) {
        this(x, y, size, buttonTexture, dynamicTexture, () -> McUtils.setScreen(openedScreen), tooltipList);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics, isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR, getX(), getY(), width, height);

        if (isHovered) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }

        if (!dynamicTexture) {
            RenderUtils.drawTexturedRect(
                    guiGraphics, buttonTexture, (int) (getX() + (width - buttonTexture.width()) / 2f), (int)
                            (getY() + (height - buttonTexture.height()) / 2f));
            return;
        }

        RenderUtils.drawHoverableTexturedRect(
                guiGraphics,
                buttonTexture,
                (int) (getX() + (width - buttonTexture.width()) / 2f),
                (int) (getY() + (height - buttonTexture.height() / 2f) / 2f),
                isHovered,
                RenderDirection.VERTICAL);
    }

    public Runnable getClickAction() {
        return clickAction;
    }

    public List<Component> getTooltipList() {
        return tooltipList;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
