/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.partymanagement.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class SuggestionPlayerWidget extends AbstractPlayerListEntryWidget {
    private final Button inviteButton;

    public SuggestionPlayerWidget(
            float x, float y, int width, int height, String playerName, boolean isOffline, float gridDivisions) {
        super((int) x, (int) y, width, height, playerName, isOffline, gridDivisions);
        this.inviteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.invite"),
                        (button) -> Models.Party.partyInvite(playerName))
                .pos((int) (this.getX() + (this.width / this.gridDivisions * 20)) + 1, this.getY())
                .size(
                        (int) ((this.getX() + (this.width / this.gridDivisions * 24))
                                        - (this.getX() + (this.width / this.gridDivisions * 20)))
                                - 2,
                        20)
                .build();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        PoseStack poseStack = guiGraphics.pose();

        // name rendering
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(playerName),
                        this.getX() + (this.width / gridDivisions * 3),
                        this.getY() + this.height / 2,
                        CommonColors.GREEN,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (Models.Party.getPartyMembers().contains(playerName)) return;
        inviteButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return inviteButton.mouseClicked(mouseX, mouseY, button);
    }
}
