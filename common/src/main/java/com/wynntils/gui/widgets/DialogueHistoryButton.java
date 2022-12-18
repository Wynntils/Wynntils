/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.WynntilsDialogueHistoryScreen;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class DialogueHistoryButton extends AbstractButton {
    public DialogueHistoryButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Dialogue History Button"));
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.DIALOGUE_BUTTON.resource(),
                this.getX(),
                this.getY(),
                this.width,
                this.height,
                Texture.DIALOGUE_BUTTON.width(),
                Texture.DIALOGUE_BUTTON.height());
    }

    @Override
    public void onPress() {
        McUtils.mc().setScreen(WynntilsDialogueHistoryScreen.create());
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
