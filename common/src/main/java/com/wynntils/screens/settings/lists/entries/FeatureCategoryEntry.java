/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.lists.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class FeatureCategoryEntry extends Entry {
    private final String category;

    public FeatureCategoryEntry(String category) {
        this.category = category;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return ImmutableList.of();
    }

    @Override
    public void render(
            PoseStack poseStack,
            int index,
            int top,
            int left,
            int width,
            int height,
            int mouseX,
            int mouseY,
            boolean isMouseOver,
            float partialTick) {}

    @Override
    public List<? extends GuiEventListener> children() {
        return ImmutableList.of();
    }

    public String getCategory() {
        return category;
    }
}
