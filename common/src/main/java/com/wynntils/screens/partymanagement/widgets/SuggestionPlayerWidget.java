/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.partymanagement.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SuggestionPlayerWidget extends AbstractWidget {

    private final String playerName;
    private final Button inviteButton;

    public SuggestionPlayerWidget(int x, int y, int width, int height, String playerName) {
        super(x, y, width, height, Component.literal(playerName));
        this.playerName = playerName;
        this.inviteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.invite"),
                        (button) -> Models.Party.partyInvite(playerName))
                .pos(this.getX() + 130, this.getY())
                .size(40, 20)
                .build();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        PlayerInfo playerInfo =
                McUtils.mc().getConnection().getPlayerInfo(playerName); // Disconnected players will just be steves
        ResourceLocation skin =
                (playerInfo == null) ? new ResourceLocation("textures/entity/steve.png") : playerInfo.getSkinLocation();
        // head rendering
        RenderUtils.drawTexturedRect(poseStack, skin, this.getX(), this.getY(), 8, 16, 16, 8, 8, 8, 8, 64, 64);
        RenderUtils.drawTexturedRect(poseStack, skin, this.getX(), this.getY(), 8, 16, 16, 40, 8, 8, 8, 64, 64);

        // name rendering
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        playerName,
                        this.getX() + 36,
                        this.getY() + 8,
                        CommonColors.GREEN,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);

        if (Models.Party.getPartyMembers().contains(playerName)) return;
        inviteButton.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return inviteButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
