/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.maps.CustomSeaskipperScreen;
import com.wynntils.services.map.pois.SeaskipperDestinationPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SeaskipperDestinationButton extends WynntilsButton {
    private final SeaskipperDestinationPoi destination;
    private final CustomSeaskipperScreen seaskipperScreen;

    public SeaskipperDestinationButton(
            int x,
            int y,
            int width,
            int height,
            SeaskipperDestinationPoi destination,
            CustomSeaskipperScreen seaskipperScreen) {
        super(x, y, width, height, Component.literal("Destination Button"));
        this.destination = destination;
        this.seaskipperScreen = seaskipperScreen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.DESTINATION_BUTTON.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                this.isHovered || seaskipperScreen.getSelectedDestination() == destination
                        ? Texture.DESTINATION_BUTTON.height() / 2
                        : 0,
                Texture.DESTINATION_BUTTON.width(),
                Texture.DESTINATION_BUTTON.height() / 2,
                Texture.DESTINATION_BUTTON.width(),
                Texture.DESTINATION_BUTTON.height());

        float x = this.getX() + this.width * 0.05f;
        float y = this.getY() + this.height * 0.16f;

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.customSeaskipperScreen.destination",
                                destination.getName(),
                                destination.getLevel())),
                        x,
                        y + 1,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE,
                        this.height * 0.032f);

        CustomColor priceColor;

        if (Models.Emerald.getAmountInInventory()
                >= destination.getDestination().item().getPrice()) {
            priceColor = CommonColors.GREEN;
        } else {
            priceColor = CommonColors.RED;
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.customSeaskipperScreen.cost",
                                destination.getDestination().item().getPrice())),
                        x,
                        y + 10,
                        priceColor,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE,
                        this.height * 0.032f);

        if (isHovered) {
            List<Component> tooltip;

            if (seaskipperScreen.getSelectedDestination() == destination) {
                tooltip = List.of(Component.translatable(
                                "screens.wynntils.customSeaskipperScreen.travelToDestination", destination.getName())
                        .withStyle(ChatFormatting.GRAY));
            } else {
                tooltip = List.of(
                        Component.translatable("screens.wynntils.customSeaskipperScreen.select", destination.getName())
                                .withStyle(ChatFormatting.GRAY));
            }

            McUtils.screen().setTooltipForNextRenderPass(Lists.transform(tooltip, Component::getVisualOrderText));
        }
    }

    @Override
    public void onPress() {}

    public SeaskipperDestinationPoi getDestination() {
        return destination;
    }
}
