/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.UrlId;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.Map;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GuideGearItemStackButton extends WynntilsButton {
    private final GuideGearItemStack itemStack;
    private final WynntilsItemGuideScreen screen;

    public GuideGearItemStackButton(
            int x, int y, int width, int height, GuideGearItemStack itemStack, WynntilsItemGuideScreen screen) {
        super(x, y, width, height, Component.literal("Guide GearItemStack Button"));
        this.itemStack = itemStack;
        this.screen = screen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor color = CustomColor.fromChatFormatting(
                itemStack.getGearProfile().getTier().getChatFormatting());

        float actualX = screen.getTranslationX() + getX();
        float actualY = screen.getTranslationY() + getY();

        RenderUtils.drawTexturedRectWithColor(
                Texture.HIGHLIGHT.resource(),
                color.withAlpha(1f),
                actualX - 1,
                actualY - 1,
                0,
                18,
                18,
                Texture.HIGHLIGHT.width(),
                Texture.HIGHLIGHT.height());

        RenderUtils.renderGuiItem(itemStack, (int) (actualX), (int) (actualY), 1f);

        if (Managers.Favorites.isFavorite(itemStack)) {
            RenderUtils.drawScalingTexturedRect(
                    poseStack,
                    Texture.FAVORITE.resource(),
                    getX() + 12,
                    getY() - 4,
                    200,
                    9,
                    9,
                    Texture.FAVORITE.width(),
                    Texture.FAVORITE.height());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) && !KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            return false;
        }

        String unformattedName = ComponentUtils.getUnformatted(itemStack.getHoverName());
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Managers.Net.openLink(UrlId.LINK_WYNNDATA_ITEM_LOOKUP, Map.of("itemname", unformattedName));
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Managers.Favorites.toggleFavorite(unformattedName);
            Managers.Config.saveConfig();
        }

        return true;
    }

    /* no-op */
    @Override
    public void onPress() {}

    public GuideGearItemStack getItemStack() {
        return itemStack;
    }
}
