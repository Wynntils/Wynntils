/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.abilities.widgets;

import com.wynntils.models.abilitytree.type.AbilityTreeConnectionNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class AbilityNodeConnectionWidget extends AbstractWidget {
    public static final int SIZE = 20;

    private final AbilityTreeConnectionNode node;

    public AbilityNodeConnectionWidget(int x, int y, int width, int height, AbilityTreeConnectionNode node) {
        super(x, y, width, height, Component.literal("Connection Node"));
        this.node = node;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.renderItem(node.getItemStack(), this.getX(), this.getY());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public AbilityTreeConnectionNode getNode() {
        return node;
    }
}
