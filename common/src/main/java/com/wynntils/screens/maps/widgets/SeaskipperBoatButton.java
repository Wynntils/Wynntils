/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SeaskipperBoatButton extends WynntilsButton {
    private static final List<Component> TOOLTIP = List.of(
            Component.literal("[>] ")
                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("screens.wynntils.seaskipperMapGui.buyBoat.name")),
            Component.translatable("screens.wynntils.seaskipperMapGui.buyBoat.description")
                    .withStyle(ChatFormatting.GRAY));

    public SeaskipperBoatButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Boat Button"));
    }

    @Override
    public void onPress() {
        Models.Seaskipper.purchaseBoat();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.BOAT_BUTTON.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                !isHovered ? Texture.BOAT_BUTTON.height() / 2 : 0,
                Texture.BOAT_BUTTON.width(),
                Texture.BOAT_BUTTON.height() / 2,
                Texture.BOAT_BUTTON.width(),
                Texture.BOAT_BUTTON.height());

        if (isHovered) {
            McUtils.mc().screen.setTooltipForNextRenderPass(Lists.transform(TOOLTIP, Component::getVisualOrderText));
        }
    }
}
