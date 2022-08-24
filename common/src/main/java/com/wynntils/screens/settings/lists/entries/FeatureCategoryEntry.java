/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.lists.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.render.FontRenderer;
import java.util.List;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class FeatureCategoryEntry extends FeatureListEntryBase {
    private static final int ITEM_HEIGHT = 15;
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
            float partialTick) {

        poseStack.pushPose();

        poseStack.translate(left, top, 0);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        this.category,
                        0,
                        width,
                        0,
                        width - 10,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);

        poseStack.popPose();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return ImmutableList.of();
    }

    public String getCategory() {
        return category;
    }

    public static int getItemHeight() {
        return ITEM_HEIGHT;
    }

    @Override
    public int getRenderHeight() {
        return FeatureCategoryEntry.getItemHeight();
    }
}
