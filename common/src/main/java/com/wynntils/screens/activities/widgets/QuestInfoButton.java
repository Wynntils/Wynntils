/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.network.chat.Component;

public class QuestInfoButton extends WynntilsButton {
    public QuestInfoButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Quest Info / Mini Quest info"));
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.QUESTS_BUTTON.resource(),
                this.getX(),
                this.getY(),
                this.width,
                this.height,
                Texture.QUESTS_BUTTON.width(),
                Texture.QUESTS_BUTTON.height());
    }

    @Override
    public void onPress() {}
}
