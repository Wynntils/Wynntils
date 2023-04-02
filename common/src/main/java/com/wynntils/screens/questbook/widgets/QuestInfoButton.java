/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.questbook.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.questbook.WynntilsQuestBookScreen;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.network.chat.Component;

public class QuestInfoButton extends WynntilsButton {
    private final WynntilsQuestBookScreen questBookScreen;

    public QuestInfoButton(int x, int y, int width, int height, WynntilsQuestBookScreen questBookScreen) {
        super(x, y, width, height, Component.literal("Quest Info / Mini Quest Toggle Button"));
        this.questBookScreen = questBookScreen;
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
    public void onPress() {
        this.questBookScreen.setMiniQuestMode(!this.questBookScreen.isMiniQuestMode());
    }
}
