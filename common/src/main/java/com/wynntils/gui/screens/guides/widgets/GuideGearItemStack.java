/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.guides.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlId;
import com.wynntils.features.user.ItemFavoriteFeature;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.guides.WynntilsItemGuideScreen;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.wynn.item.GearItemStack;
import java.util.Map;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

public class GuideGearItemStack extends AbstractButton {
    private final GearItemStack itemStack;
    private final WynntilsItemGuideScreen screen;

    public GuideGearItemStack(
            int x, int y, int width, int height, GearItemStack itemStack, WynntilsItemGuideScreen screen) {
        super(x, y, width, height, new TextComponent("Guide GearItemStack Button"));
        this.itemStack = itemStack;
        this.screen = screen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor color = itemStack.getItemProfile().getTier().getHighlightColor();

        float actualX = screen.getTranslationX() + x;
        float actualY = screen.getTranslationY() + y;

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

        String unformattedName = ComponentUtils.getUnformatted(itemStack.getHoverName());
        if (ItemFavoriteFeature.INSTANCE.favoriteItems.contains(unformattedName)) {
            RenderUtils.drawScalingTexturedRect(
                    poseStack,
                    Texture.FAVORITE.resource(),
                    x + 12,
                    y - 4,
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
            NetManager.openLink(UrlId.LINK_WYNNDATA_ITEM_LOOKUP, Map.of("itemname", unformattedName));
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
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    public GearItemStack getItemStack() {
        return itemStack;
    }
}
