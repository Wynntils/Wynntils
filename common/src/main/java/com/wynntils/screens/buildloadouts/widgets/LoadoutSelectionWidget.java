/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class LoadoutSelectionWidget extends AbstractButton implements TooltipProvider {
    private final StyledText text;
    private final int x;
    private final int y;
    private final Texture icon;
    private boolean clicked = false;

    public LoadoutSelectionWidget(StyledText text, Texture icon, int x, int y) {
        super(x, y, 133 - 10, 39, Component.literal("Loadout Selection Button"));
        this.text = text;
        this.icon = icon;
        this.x = x;
        this.y = y;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);
        if (!clicked) {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                    x,
                    y,
                    this.width,
                    this.height);
        } else {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_BLUE,
                    x,
                    y,
                    this.width,
                    this.height);

            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_WIDGET_SELECT_TAB,
                    x + this.width - Texture.BUILD_LOADOUTS_WIDGET_SELECT_TAB.width() / 2f,
                    (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_WIDGET_SELECT_TAB.height() / 2f);
        }

        RenderUtils.drawTexturedRect(
                guiGraphics,
                icon,
                this.x + icon.width() / 2f,
                (this.y + this.height / 2f) - icon.height() / 2f);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        this.text,
                        (this.x + this.width / 2f) + 22,
                        (this.y + this.height / 2f) - 4,
                        70,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public void onPress(InputWithModifiers input) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return false;
        clicked = !clicked;
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return List.of();
    }
}
