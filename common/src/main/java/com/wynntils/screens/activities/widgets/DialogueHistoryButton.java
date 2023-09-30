/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.activities.WynntilsDialogueHistoryScreen;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DialogueHistoryButton extends WynntilsButton {
    public DialogueHistoryButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Dialogue History Button"));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.DIALOGUE_ICON.resource(),
                this.getX(),
                this.getY(),
                this.width,
                this.height,
                Texture.DIALOGUE_ICON.width(),
                Texture.DIALOGUE_ICON.height());
    }

    @Override
    public void onPress() {
        McUtils.mc().setScreen(WynntilsDialogueHistoryScreen.create());
    }
}
