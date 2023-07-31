/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.powder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GuidePowderItemStackButton extends WynntilsButton {
    private final GuidePowderItemStack itemStack;

    public GuidePowderItemStackButton(
            int x, int y, int width, int height, GuidePowderItemStack itemStack, WynntilsPowderGuideScreen screen) {
        super(x, y, width, height, Component.literal("Guide PowderItemStack Button"));
        this.itemStack = itemStack;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor color = itemStack.getElement().getColor();

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

        RenderUtils.renderItem(poseStack, itemStack, getX(), getY());

        poseStack.pushPose();
        poseStack.translate(0, 0, 200);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(MathUtils.toRoman(itemStack.getTier())),
                        getX() + 2,
                        getX() + 14,
                        getY() + 8,
                        0,
                        color,
                        HorizontalAlignment.CENTER,
                        TextShadow.OUTLINE);
        poseStack.popPose();

        if (Services.Favorites.isFavorite(itemStack)) {
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

        String unformattedName =
                StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Services.Favorites.toggleFavorite(unformattedName);
            Managers.Config.saveConfig();
        }

        return true;
    }

    /* no-op */
    @Override
    public void onPress() {}

    public GuidePowderItemStack getItemStack() {
        return itemStack;
    }
}
