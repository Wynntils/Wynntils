/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gear;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GuideGearItemStackButton extends WynntilsButton {
    private final GuideGearItemStack itemStack;

    public GuideGearItemStackButton(
            int x, int y, int width, int height, GuideGearItemStack itemStack, WynntilsItemGuideScreen screen) {
        super(x, y, width, height, Component.literal("Guide GearItemStack Button"));
        this.itemStack = itemStack;
        // Things like our current class, or other requirement fulfillments can have changed,
        // so we need to redo this even if it's already done
        itemStack.buildTooltip();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor color =
                CustomColor.fromChatFormatting(itemStack.getGearInfo().tier().getChatFormatting());

        RenderUtils.drawTexturedRectWithColor(
                guiGraphics,
                Texture.HIGHLIGHT.resource(),
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
                    Texture.FAVORITE_ICON,
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

        String unformattedName =
                StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting();
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Managers.Net.openLink(UrlId.LINK_WYNNCRAFT_ITEM_LOOKUP, Map.of("itemname", unformattedName));
            return true;
        } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Services.Favorites.toggleFavorite(unformattedName);
        }

        return true;
    }

    /* no-op */
    @Override
    public void onPress(InputWithModifiers input) {}

    public GuideGearItemStack getItemStack() {
        return itemStack;
    }
}
