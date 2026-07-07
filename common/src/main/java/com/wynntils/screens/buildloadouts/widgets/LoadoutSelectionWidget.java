/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.LoadoutCategory;
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
    private LoadoutCategory loadoutCategory;
    private BuildLoadoutsScreen parent;

    public LoadoutSelectionWidget(StyledText text, Texture icon, LoadoutCategory loadoutCategory, int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 133 - 10, 31, Component.literal("Loadout Selection Button"));
        this.text = text;
        this.icon = icon;
        this.loadoutCategory = loadoutCategory;
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);
        if (parent.currentCategory != loadoutCategory) {
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
                this.x + 5,
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
        parent.currentCategory = loadoutCategory;
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return List.of();
    }
}
