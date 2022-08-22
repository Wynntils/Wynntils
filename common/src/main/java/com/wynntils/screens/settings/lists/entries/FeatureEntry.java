/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.lists.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.render.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class FeatureEntry extends Entry {
    private static final int ITEM_HEIGHT = 25;

    private final Feature feature;

    public FeatureEntry(Feature feature) {
        this.feature = feature;
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

        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.FEATURE_BUTTON.resource(),
                0,
                0,
                0,
                width,
                height,
                0,
                0,
                Texture.FEATURE_BUTTON.width(),
                Texture.FEATURE_BUTTON.height(),
                Texture.FEATURE_BUTTON.width(),
                Texture.FEATURE_BUTTON.height());

        FontRenderer.getInstance()
                .renderTextWithAlignment(
                        poseStack,
                        9f,
                        4f,
                        new TextRenderTask(
                                this.feature.getTranslatedName(),
                                new TextRenderSetting(
                                        width - 10,
                                        CommonColors.WHITE,
                                        FontRenderer.TextAlignment.LEFT_ALIGNED,
                                        FontRenderer.TextShadow.OUTLINE)),
                        width,
                        height,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle);

        poseStack.popPose();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return ImmutableList.of();
    }

    public Feature getFeature() {
        return feature;
    }

    public static int getItemHeight() {
        return ITEM_HEIGHT;
    }
}
