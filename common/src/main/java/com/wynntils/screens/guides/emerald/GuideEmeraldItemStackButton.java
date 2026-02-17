/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emerald;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GuideEmeraldItemStackButton extends WynntilsButton {
    private final GuideEmeraldItemStack itemStack;

    public GuideEmeraldItemStackButton(
            int x, int y, int width, int height, GuideEmeraldItemStack itemStack, WynntilsEmeraldGuideScreen screen) {
        super(x, y, width, height, Component.literal("Guide EmeraldPouchItemStack Button"));
        this.itemStack = itemStack;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor color = CustomColor.fromChatFormatting(ChatFormatting.GREEN);

        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.HIGHLIGHT.identifier(),
                color,
                getX() - 1,
                getY() - 1,
                18,
                18,
                0,
                0,
                18,
                18,
                Texture.HIGHLIGHT.width(),
                Texture.HIGHLIGHT.height());

        RenderUtils.renderItem(guiGraphics, itemStack, getX(), getY());

        if (Services.Favorites.isFavorite(itemStack)) {
            RenderUtils.drawScalingTexturedRect(
                    guiGraphics,
                    Texture.FAVORITE_ICON.identifier(),
                    getX() + 12,
                    getY() - 4,
                    9,
                    9,
                    Texture.FAVORITE_ICON.width(),
                    Texture.FAVORITE_ICON.height());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) && !KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            return false;
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Services.Favorites.toggleFavorite(StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting());
        }

        return true;
    }

    /* no-op */
    @Override
    public void onPress(InputWithModifiers input) {}

    public GuideEmeraldItemStack getItemStack() {
        return itemStack;
    }
}
