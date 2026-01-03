/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.google.common.collect.Lists;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.maps.CustomSeaskipperScreen;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.RenderDirection;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class SeaskipperTravelButton extends WynntilsButton {
    private final CustomSeaskipperScreen seaskipperScreen;

    public SeaskipperTravelButton(int x, int y, int width, int height, CustomSeaskipperScreen seaskipperScreen) {
        super(x, y, width, height, Component.literal("Travel Button"));
        this.seaskipperScreen = seaskipperScreen;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        seaskipperScreen.travelToDestination();
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawScalingHoverableTexturedRect(
                guiGraphics,
                Texture.TRAVEL_BUTTON,
                this.getX(),
                this.getY(),
                width,
                height,
                isHovered,
                RenderDirection.VERTICAL);

        if (isHovered && seaskipperScreen.getSelectedDestination() != null) {
            List<Component> tooltip = List.of(Component.translatable(
                            "screens.wynntils.customSeaskipperScreen.travelToDestination",
                            seaskipperScreen.getSelectedDestination().getName())
                    .withStyle(ChatFormatting.GRAY));

            guiGraphics.setTooltipForNextFrame(Lists.transform(tooltip, Component::getVisualOrderText), mouseX, mouseY);
        }
    }
}
