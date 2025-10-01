/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ingredient;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.ingredients.type.IngredientTierFormatting;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GuideIngredientItemStackButton extends WynntilsButton {
    private final GuideIngredientItemStack itemStack;

    public GuideIngredientItemStackButton(
            int x,
            int y,
            int width,
            int height,
            GuideIngredientItemStack itemStack,
            WynntilsIngredientGuideScreen screen) {
        super(x, y, width, height, Component.literal("Guide IngredientItemStack Button"));
        this.itemStack = itemStack;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor color = getHighlightColor(itemStack.getIngredientInfo().tier());

        RenderUtils.drawTexturedRectWithColor(
                guiGraphics, Texture.HIGHLIGHT, getX() - 1, getY() - 1, 18, 18, 0, 0, color);

        RenderUtils.renderItem(guiGraphics, itemStack, getX(), getY());

        String unformattedName = itemStack.getIngredientInfo().name();
        if (Services.Favorites.isFavorite(unformattedName)) {
            RenderUtils.drawScalingTexturedRect(
                    guiGraphics, Texture.FAVORITE_ICON.resource(), getX() + 12, getY() - 4, 9, 9);
        }
    }

    // FIXME: This should be painted by ItemHighlightFeature instead...
    private CustomColor getHighlightColor(int tier) {
        CustomColor highlightColor = IngredientTierFormatting.fromTierNum(tier).getHighlightColor();

        if (highlightColor == null) {
            WynntilsMod.warn("Invalid ingredient tier for: "
                    + itemStack.getIngredientInfo().name() + ": " + tier);
            return CustomColor.NONE;
        }

        return highlightColor;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) && !KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            return false;
        }

        String unformattedName = itemStack.getIngredientInfo().name();
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Managers.Net.openLink(UrlId.LINK_WYNNCRAFT_ITEM_LOOKUP, Map.of("itemname", unformattedName));
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Services.Favorites.toggleFavorite(unformattedName);
        }

        return true;
    }

    /* no-op */
    @Override
    public void onPress() {}

    public GuideIngredientItemStack getItemStack() {
        return itemStack;
    }
}
