package com.wynntils.screens.partymanagement.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PartyPlayer extends WynntilsButton {

    private final String playerName;
    private final int index;
    private final boolean isOffline;

    public PartyPlayer(int x, int y, int width, int height, String playerName, int index, boolean isOffline) {
        super(x, y, width, height, Component.literal(playerName));
        this.playerName = playerName;
        this.index = index;
        this.isOffline = isOffline;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ResourceLocation skin = McUtils.mc().getConnection().getPlayerInfo(playerName).getSkinLocation();
        // head rendering
        RenderUtils.drawTexturedRect(
                poseStack,
                skin,
                this.getX(),
                this.getY(),
                8,
                16,
                16,
                8,
                8,
                8,
                8,
                64,
                64);
        // hat rendering
        RenderUtils.drawTexturedRect(
                poseStack,
                skin,
                this.getX(),
                this.getY(),
                1,
                16,
                16,
                40,
                8,
                8,
                8,
                64,
                64);

        // name rendering
        CustomColor color = CommonColors.WHITE;
        if (playerName.equals(Models.Party.getPartyLeader())) {
            color = CommonColors.YELLOW;
        } else if (Models.Friends.getFriends().contains(playerName)) {
            color = CommonColors.GREEN;
        }
        String prefix = "";
        if (isOffline) {
            prefix = "§m";
        } else if (playerName.equals(McUtils.player().getName().getString())) {
            prefix = "§l";
        }

        String formattedPlayerName = prefix + playerName;

        FontRenderer.getInstance()
                .renderText(
                poseStack,
                formattedPlayerName,
                this.getX(),
                this.getY(),
                color,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);

        // only leader can promote/kick
        if (!McUtils.player().getName().getString().equals(Models.Party.getPartyLeader())) return;

        if (playerName.equals(Models.Party.getPartyLeader())) {
            new Button.Builder(
                    Component.translatable("screens.wynntils.partyManagementGui.disband"),
                    (button) -> disbandParty())
                    .pos(this.getX() + 292, this.getY())
                    .size(50, 20)
                    .build().renderButton(poseStack, mouseX, mouseY, partialTick);
        } else {
            // Promote button
            new Button.Builder(
                    Component.translatable("screens.wynntils.partyManagementGui.promote"),
                    (button) -> promoteToLeader(playerName))
                    .pos(this.getX(), this.getY())
                    .size(50, 20)
                    .build().render(poseStack, mouseX, mouseY, partialTick);

            // Kick button
            new Button.Builder(
                    Component.translatable("screens.wynntils.partyManagementGui.kick"),
                    (button) -> kickFromParty(playerName))
                    .pos(this.getX(), this.getY())
                    .size(50, 20)
                    .build().renderButton(poseStack, mouseX, mouseY, partialTick);
        }
    }

    private void kickFromParty(String playerName) {
        McUtils.sendCommand("party kick " + playerName);
    }

    private void promoteToLeader(String playerName) {
        McUtils.sendCommand("party promote " + playerName);
    }

    private void disbandParty() {
        McUtils.sendCommand("party disband");
    }

    @Override
    public void onPress() {
        // do nothing
    }
}
