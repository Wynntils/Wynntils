/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.network.chat.Component;

public class DiscoveryProgressButton extends WynntilsButton implements TooltipProvider {
    private final boolean isSecretDiscoveryButton;

    public DiscoveryProgressButton(int x, int y, int width, int height, boolean isSecretDiscoveryButton) {
        super(x, y, width, height, Component.literal("Discovery Progress Button"));

        this.isSecretDiscoveryButton = isSecretDiscoveryButton;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
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

    private boolean isSecretDiscoveryButton() {
        return isSecretDiscoveryButton;
    }

    @Override
    public List<Component> getTooltipLines() {
        if (isSecretDiscoveryButton()) {
            return Models.Discovery.getSecretDiscoveriesTooltip();
        } else {
            return Models.Discovery.getDiscoveriesTooltip();
        }
    }
}
