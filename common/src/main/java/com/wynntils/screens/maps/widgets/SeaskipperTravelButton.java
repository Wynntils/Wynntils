/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.maps.SeaskipperDepartureBoardScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SeaskipperTravelButton extends WynntilsButton {
    private final SeaskipperDepartureBoardScreen departureBoardScreen;

    public SeaskipperTravelButton(
            int x, int y, int width, int height, SeaskipperDepartureBoardScreen departureBoardScreen) {
        super(x, y, width, height, Component.literal("Travel Button"));
        this.departureBoardScreen = departureBoardScreen;
    }

    @Override
    public void onPress() {
        departureBoardScreen.travelToDestination();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.TRAVEL_BUTTON.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                departureBoardScreen.hasSelectedDestination() ? 0 : Texture.TRAVEL_BUTTON.height() / 2,
                Texture.TRAVEL_BUTTON.width(),
                Texture.TRAVEL_BUTTON.height() / 2,
                Texture.TRAVEL_BUTTON.width(),
                Texture.TRAVEL_BUTTON.height());

        if (isHovered && departureBoardScreen.hasSelectedDestination()) {
            List<Component> tooltip =
                    List.of(Component.translatable("screens.wynntils.seaskipperMapGui.travelToDestination")
                            .withStyle(ChatFormatting.GRAY));

            McUtils.mc().screen.setTooltipForNextRenderPass(Lists.transform(tooltip, Component::getVisualOrderText));
        }
    }
}
