/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.WynntilsQuestBookScreen;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class QuestInfoButton extends AbstractButton {
    private final WynntilsQuestBookScreen questBookScreen;

    public QuestInfoButton(int x, int y, int width, int height, WynntilsQuestBookScreen questBookScreen) {
        super(x, y, width, height, Component.literal("Quest Info / Mini Quest Toggle Button"));
        this.questBookScreen = questBookScreen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.QUESTS_BUTTON.resource(),
                this.x,
                this.y,
                this.width,
                this.height,
                Texture.QUESTS_BUTTON.width(),
                Texture.QUESTS_BUTTON.height());
    }

    @Override
    public void onPress() {
        this.questBookScreen.setMiniQuestMode(!this.questBookScreen.isMiniQuestMode());
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
