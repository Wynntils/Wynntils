/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.partymanagement.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PartyMemberWidget extends AbstractWidget {

    private final String playerName;
    private final boolean isOffline;
    private final Button promoteButton;
    private final Button kickButton;
    private final Button disbandButton;

    public PartyMemberWidget(int x, int y, int width, int height, String playerName, boolean isOffline) {
        super(x, y, width, height, Component.literal(playerName));
        this.playerName = playerName;
        this.isOffline = isOffline;
        this.promoteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.promote"),
                        (button) -> Models.Party.promoteToLeader(playerName))
                .pos(this.getX() + 240, this.getY())
                .size(50, 20)
                .build();
        this.kickButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.kick"),
                        (button) -> Models.Party.kickFromParty(playerName))
                .pos(this.getX() + 292, this.getY())
                .size(50, 20)
                .build();
        this.disbandButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.disband"),
                        (button) -> Models.Party.disbandParty())
                .pos(this.getX() + 292, this.getY())
                .size(50, 20)
                .build();
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ResourceLocation skin =
                McUtils.mc().getConnection().getPlayerInfo(playerName).getSkinLocation();
        // head rendering
        RenderUtils.drawTexturedRect(poseStack, skin, this.getX(), this.getY(), 8, 16, 16, 8, 8, 8, 8, 64, 64);
        // hat rendering
        RenderUtils.drawTexturedRect(poseStack, skin, this.getX(), this.getY(), 1, 16, 16, 40, 8, 8, 8, 64, 64);

        // name rendering
        CustomColor color = CommonColors.WHITE;
        if (playerName.equals(Models.Party.getPartyLeader())) {
            color = CommonColors.YELLOW;
        } else if (Models.Friends.getFriends().contains(playerName)) {
            color = CommonColors.GREEN;
        }
        String prefix = "";
        if (isOffline) {
            prefix = ChatFormatting.STRIKETHROUGH.toString();
        } else if (playerName.equals(McUtils.player().getName().getString())) {
            prefix = ChatFormatting.BOLD.toString();
        }

        String formattedPlayerName = prefix + playerName;

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        formattedPlayerName,
                        this.getX() + 36,
                        this.getY() + 8,
                        color,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);

        // only leader can promote/kick
        if (!McUtils.player().getName().getString().equals(Models.Party.getPartyLeader())) return;

        if (playerName.equals(Models.Party.getPartyLeader())) {
            disbandButton.render(poseStack, mouseX, mouseY, partialTick);
        } else {
            promoteButton.render(poseStack, mouseX, mouseY, partialTick);
            kickButton.render(poseStack, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return promoteButton.mouseClicked(mouseX, mouseY, button)
                || kickButton.mouseClicked(mouseX, mouseY, button)
                || disbandButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
