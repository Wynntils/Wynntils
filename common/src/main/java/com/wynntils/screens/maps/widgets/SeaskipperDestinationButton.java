/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.services.map.pois.SeaskipperDestinationPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.network.chat.Component;

public class SeaskipperDestinationButton extends WynntilsButton {
    private final boolean selected;
    private final SeaskipperDestinationPoi destination;

    public SeaskipperDestinationButton(
            int x, int y, int width, int height, boolean selected, SeaskipperDestinationPoi destination) {
        super(x, y, width, height, Component.literal("Destination Button"));
        this.selected = selected;
        this.destination = destination;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.DESTINATION_BUTTON.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                this.isHovered || selected ? Texture.DESTINATION_BUTTON.height() / 2 : 0,
                Texture.DESTINATION_BUTTON.width(),
                Texture.DESTINATION_BUTTON.height() / 2,
                Texture.DESTINATION_BUTTON.width(),
                Texture.DESTINATION_BUTTON.height());

        poseStack.pushPose();
        poseStack.translate(this.getX() + this.width * 0.05f, this.getY() + this.height * 0.16f, 0f);
        float scale = this.height * 0.032f;
        poseStack.scale(scale, scale, 0f);

        int maxTextWidth = 90;
        String destinationName =
                RenderedStringUtils.getMaxFittingText(destination.getName(), maxTextWidth, McUtils.mc().font);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(destinationName),
                        0,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);

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
                        StyledText.fromString("Cost: %d²"
                                .formatted(destination.getDestination().item().getPrice())),
                        0,
                        10f,
                        priceColor,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        poseStack.popPose();
    }

    @Override
    public void onPress() {}

    public SeaskipperDestinationPoi getDestination() {
        return destination;
    }
}
