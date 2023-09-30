/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class QuestInfoButton extends WynntilsButton {
    public QuestInfoButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Quest Info / Mini Quest info"));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.QUESTS_SCROLL_ICON.resource(),
                this.getX(),
                this.getY(),
                this.width,
                this.height,
                Texture.QUESTS_SCROLL_ICON.width(),
                Texture.QUESTS_SCROLL_ICON.height());
    }

    @Override
    public void onPress() {}
}
