/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.partymanagement.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractPlayerListEntryWidget extends AbstractWidget {
    protected final String playerName;
    protected final boolean isOffline;
    protected final float gridDivisions;

    protected AbstractPlayerListEntryWidget(
            int x, int y, int width, int height, String playerName, boolean isOffline, float gridDivisions) {
        super(x, y, width, height, Component.literal(playerName));
        this.playerName = playerName;
        this.isOffline = false;
        this.gridDivisions = gridDivisions;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        PlayerInfo playerInfo =
                McUtils.mc().getConnection().getPlayerInfo(playerName); // Disconnected players will just be Steves
        ResourceLocation skin = (playerInfo == null)
                ? DefaultPlayerSkin.getDefaultTexture()
                : playerInfo.getSkin().texture();
        // head rendering
        RenderUtils.drawTexturedRect(
                poseStack,
                skin,
                this.getX() + (this.width / gridDivisions) - 8,
                this.getY() + (this.height / 2) - 8,
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
                this.getX() + (this.width / gridDivisions) - 8,
                this.getY() + (this.height / 2) - 8,
                8,
                16,
                16,
                40,
                8,
                8,
                8,
                64,
                64);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
