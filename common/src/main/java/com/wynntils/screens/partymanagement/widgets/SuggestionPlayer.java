package com.wynntils.screens.partymanagement.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SuggestionPlayer extends WynntilsButton {

    private final String playerName;
    private final int index;

    public SuggestionPlayer(int x, int y, int width, int height, String playerName, int index) {
        super(x, y, width, height, Component.literal(playerName));
        this.playerName = playerName;
        this.index = index;
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
        RenderUtils.drawTexturedRect(
                poseStack,
                skin,
                this.getX(),
                this.getY(),
                8,
                16,
                16,
                40,
                8,
                8,
                8,
                64,
                64);

        // name rendering
        FontRenderer.getInstance()
                .renderText(
                poseStack,
                playerName,
                this.getX(),
                this.getY(),
                CommonColors.GREEN,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);

        // Invite button
        new Button.Builder(
                Component.translatable("screens.wynntils.partyManagementGui.invite"),
                (button) -> inviteToParty(playerName))
                .pos(this.getX(), this.getY())
                .size(40, 20)
                .build().render(poseStack, mouseX, mouseY, partialTick);
    }

    private void inviteToParty(String playerName) {
        McUtils.sendCommand("party invite " + playerName);
    }

    @Override
    public void onPress() {
        // nothing
    }
}
