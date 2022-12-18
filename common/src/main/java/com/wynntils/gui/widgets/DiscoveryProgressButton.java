/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class DiscoveryProgressButton extends AbstractButton {
    private final boolean isSecretDiscoveryButton;

    public DiscoveryProgressButton(int x, int y, int width, int height, boolean isSecretDiscoveryButton) {
        super(x, y, width, height, Component.literal("Discovery Progress Button"));

        this.isSecretDiscoveryButton = isSecretDiscoveryButton;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Texture texture = isSecretDiscoveryButton ? Texture.SECRET_DISCOVERIES_ICON : Texture.DISCOVERIES_ICON;

        if (this.isHovered) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    texture.resource(),
                    getX() + (width - texture.width()) / 2f,
                    getY() + (height - texture.height() / 2f) / 2f,
                    1,
                    texture.width(),
                    texture.height() / 2f,
                    0,
                    texture.height() / 2,
                    texture.width(),
                    texture.height() / 2,
                    texture.width(),
                    texture.height());
        } else {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    texture.resource(),
                    getX() + (width - texture.width()) / 2f,
                    getY() + (height - texture.height() / 2f) / 2f,
                    1,
                    texture.width(),
                    texture.height() / 2f,
                    0,
                    0,
                    texture.width(),
                    texture.height() / 2,
                    texture.width(),
                    texture.height());
        }
    }

    @Override
    public void onPress() {}

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    public boolean isSecretDiscoveryButton() {
        return isSecretDiscoveryButton;
    }
}
