/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class LoadoutMenuDeleteButton extends AbstractButton {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;

    public LoadoutMenuDeleteButton(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 79, 20, Component.literal("Loadout Menu Delete Button"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_RED,
                x,
                y,
                this.width,
                this.height);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Delete"),
                            (this.x + this.width / 2f),
                            (this.y + this.height / 2f),
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
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        Loadout selected = parent.getSelectedLoadout();
        Services.loadout.deleteLoadout(selected.name());
        parent.setSelectedLoadout(null);
        parent.loadoutScrollListWidget.scrollOffset = 0;
        parent.loadoutScrollListWidget.populateLoadouts();

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
