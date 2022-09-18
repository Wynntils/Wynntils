/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.screens.WynntilsQuestBookScreen;
import com.wynntils.wynn.model.questbook.QuestBookManager;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public class ReloadQuestsButton extends AbstractButton {
    private final WynntilsQuestBookScreen screen;

    public ReloadQuestsButton(int x, int y, int width, int height, WynntilsQuestBookScreen screen) {
        super(x, y, width, height, new TextComponent("Reload Quests Button"));
        this.screen = screen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Texture reloadButton = Texture.RELOAD_BUTTON;
        if (this.isHovered) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    reloadButton.resource(),
                    this.x,
                    this.y,
                    0,
                    this.width,
                    this.height,
                    reloadButton.width() / 2f,
                    0,
                    reloadButton.width() / 2f,
                    reloadButton.height(),
                    reloadButton.width(),
                    reloadButton.height());
        } else {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    reloadButton.resource(),
                    this.x,
                    this.y,
                    0,
                    this.width,
                    this.height,
                    0,
                    0,
                    reloadButton.width() / 2f,
                    reloadButton.height(),
                    reloadButton.width(),
                    reloadButton.height());
        }
    }

    @Override
    public void onPress() {
        QuestBookManager.rescanQuestBook();
        screen.setQuests(QuestBookManager.getQuests());
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
