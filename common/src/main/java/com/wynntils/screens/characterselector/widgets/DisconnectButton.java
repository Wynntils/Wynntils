/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.characterselector.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.characterselector.CharacterSelectorScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class DisconnectButton extends WynntilsButton {
    private static final List<Component> TOOLTIP = List.of(
            Component.translatable("screens.wynntils.characterSelection.disconnectButton.disconnect")
                    .withStyle(ChatFormatting.GREEN),
            Component.translatable("screens.wynntils.characterSelection.disconnectButton.description")
                    .withStyle(ChatFormatting.GRAY));
    private final CharacterSelectorScreen characterSelectorScreen;

    public DisconnectButton(int x, int y, int width, int height, CharacterSelectorScreen characterSelectorScreen) {
        super(x, y, width, height, Component.literal("Disconnect Button"));
        this.characterSelectorScreen = characterSelectorScreen;
    }

    @Override
    public void onPress() {
        // This is replicating the behavior found in PauseScreen.onDisconnect()
        TitleScreen titleScreen = new TitleScreen();
        McUtils.mc().level.disconnect();
        McUtils.mc().clearLevel();
        McUtils.mc().setScreen(titleScreen);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.DISCONNECT_BUTTON.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                isHovered ? 0 : Texture.DISCONNECT_BUTTON.height() / 2,
                Texture.DISCONNECT_BUTTON.width(),
                Texture.DISCONNECT_BUTTON.height() / 2,
                Texture.DISCONNECT_BUTTON.width(),
                Texture.DISCONNECT_BUTTON.height());

        if (isHovered) {
            McUtils.mc().screen.setTooltipForNextRenderPass(Lists.transform(TOOLTIP, Component::getVisualOrderText));
        }
    }
}
