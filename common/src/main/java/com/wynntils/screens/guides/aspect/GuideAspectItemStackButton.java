/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.aspect;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GuideAspectItemStackButton extends WynntilsButton {
    private final GuideAspectItemStack itemStack;

    public GuideAspectItemStackButton(
            int x, int y, int width, int height, GuideAspectItemStack itemStack, WynntilsAspectGuideScreen screen) {
        super(x, y, width, height, Component.literal("Guide AspectItemStack Button"));
        this.itemStack = itemStack;
        // Things like our current class, or other requirement fulfillments could have changed,
        // so we need to redo this even if it's already done
        itemStack.buildTooltip();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        CustomColor color = CustomColor.fromChatFormatting(
                itemStack.getAspectInfo().gearTier().getChatFormatting());

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
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_AQUA),
                        HorizontalAlignment.CENTER,
                        TextShadow.OUTLINE);
        poseStack.popPose();

        if (Services.Favorites.isFavorite(itemStack)) {
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) && !KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            return false;
        }

        String unformattedName =
                StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Services.Favorites.toggleFavorite(unformattedName);
        }

        return true;
    }

    /* no-op */
    @Override
    public void onPress() {}

    public GuideAspectItemStack getItemStack() {
        return itemStack;
    }
}
