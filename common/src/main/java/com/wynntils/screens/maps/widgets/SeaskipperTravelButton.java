/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.models.map.pois.SeaskipperDestinationPoi;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class SeaskipperTravelButton extends WynntilsButton {
    private List<Component> tooltip;
    private SeaskipperDestinationPoi selectedPoi = null;

    public SeaskipperTravelButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Travel Button"));
    }

    @Override
    public void onPress() {
        if (selectedPoi != null) {
            Models.Seaskipper.purchasePass(selectedPoi.getDestination());
        }
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.TRAVEL_BUTTON.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                selectedPoi == null ? Texture.TRAVEL_BUTTON.height() / 2 : 0,
                Texture.TRAVEL_BUTTON.width(),
                Texture.TRAVEL_BUTTON.height() / 2,
                Texture.TRAVEL_BUTTON.width(),
                Texture.TRAVEL_BUTTON.height());

        if (isHovered && selectedPoi != null) {
            tooltip = List.of(Component.translatable(
                            "screens.wynntils.seaskipperMapGui.travelToDestination", selectedPoi.getName())
                    .withStyle(ChatFormatting.DARK_GRAY));

            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY - TooltipUtils.getToolTipHeight(TooltipUtils.componentToClientTooltipComponent(tooltip)),
                    0,
                    tooltip,
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }

    public void setSelectedPoi(SeaskipperDestinationPoi selectedPoi) {
        this.selectedPoi = selectedPoi;
    }
}
