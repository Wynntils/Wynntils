/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.guides.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.features.user.ItemFavoriteFeature;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.guides.WynntilsIngredientGuideScreen;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.utils.Utils;
import com.wynntils.wynn.item.IngredientItemStack;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GuideIngredientItemStack extends AbstractButton {
    private final IngredientItemStack itemStack;
    private final WynntilsIngredientGuideScreen screen;

    public GuideIngredientItemStack(
            int x, int y, int width, int height, IngredientItemStack itemStack, WynntilsIngredientGuideScreen screen) {
        super(x, y, width, height, Component.literal("Guide IngredientItemStack Button"));
        this.itemStack = itemStack;
        this.screen = screen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor color = itemStack.getIngredientProfile().getTier().getHighlightColor();

        float actualX = screen.getTranslationX() + this.getX();
        float actualY = screen.getTranslationY() + this.getY();

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

        String unformattedName = itemStack.getIngredientProfile().getDisplayName();
        if (ItemFavoriteFeature.INSTANCE.favoriteItems.contains(unformattedName)) {
            RenderUtils.drawScalingTexturedRect(
                    poseStack,
                    Texture.FAVORITE.resource(),
                    this.getX() + 12,
                    this.getY() - 4,
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

        String unformattedName = itemStack.getIngredientProfile().getDisplayName();
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Utils.openUrl("https://www.wynndata.tk/i/" + Utils.encodeUrl(unformattedName));
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (ItemFavoriteFeature.INSTANCE.favoriteItems.contains(unformattedName)) {
                ItemFavoriteFeature.INSTANCE.favoriteItems.remove(unformattedName);
            } else {
                ItemFavoriteFeature.INSTANCE.favoriteItems.add(unformattedName);
            }

            ConfigManager.saveConfig();
        }

        return true;
    }

    /* no-op */
    @Override
    public void onPress() {}

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public IngredientItemStack getItemStack() {
        return itemStack;
    }
}
