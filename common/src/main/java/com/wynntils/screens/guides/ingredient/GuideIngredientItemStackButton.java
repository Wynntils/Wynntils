/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
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
        PoseStack poseStack = guiGraphics.pose();

        CustomColor color = getHighlightColor(itemStack.getIngredientInfo().tier());

        RenderUtils.drawTexturedRectWithColor(
                poseStack,
                Texture.HIGHLIGHT.resource(),
                color.withAlpha(1f),
                getX() - 1,
                getY() - 1,
                0,
                18,
                18,
                Texture.HIGHLIGHT.width(),
                Texture.HIGHLIGHT.height());

        RenderUtils.renderItem(guiGraphics, itemStack, getX(), getY());

        String unformattedName = itemStack.getIngredientInfo().name();
        if (Services.Favorites.isFavorite(unformattedName)) {
            RenderUtils.drawScalingTexturedRect(
                    poseStack,
                    Texture.FAVORITE_ICON.resource(),
                    getX() + 12,
                    getY() - 4,
                    200,
                    9,
                    9,
                    Texture.FAVORITE_ICON.width(),
                    Texture.FAVORITE_ICON.height());
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
