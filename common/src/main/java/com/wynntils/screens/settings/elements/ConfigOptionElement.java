/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.screens.settings.widgets.FeatureSettingWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public abstract class ConfigOptionElement {
    protected static final CustomColor BORDER_COLOR = new CustomColor(73, 62, 50, 255);
    protected static final CustomColor FOREGROUND_COLOR = new CustomColor(137, 117, 92, 255);

    protected final ConfigHolder configHolder;
    protected final FeatureSettingWidget featureSettingWidget;

    public ConfigOptionElement(ConfigHolder configHolder, FeatureSettingWidget featureSettingWidget) {
        this.configHolder = configHolder;
        this.featureSettingWidget = featureSettingWidget;
    }

    public void render(
            PoseStack poseStack,
            float x,
            float y,
            float width,
            float height,
            int mouseX,
            int mouseY,
            float partialTick) {
        poseStack.pushPose();

        poseStack.translate(x, y, 0);

        renderBackground(poseStack, width, height);

        renderConfigTitle(poseStack, width, height);

        renderDescription(poseStack, width, height);

        renderConfigAppropriateButton(poseStack, width, height, mouseX, mouseY, partialTick);

        poseStack.popPose();
    }

    protected void renderDescription(PoseStack poseStack, float width, float height) {
        float oneThirdOfWidth = width / 3f;
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        configHolder.getDescription(),
                        5,
                        oneThirdOfWidth,
                        0,
                        height,
                        oneThirdOfWidth,
                        CommonColors.WHITE,
                        VerticalAlignment.Middle,
                        FontRenderer.TextAlignment.LEFT_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);
    }

    protected void renderConfigTitle(PoseStack poseStack, float width, float height) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        configHolder.getDisplayName(),
                        0,
                        width,
                        3,
                        height,
                        width,
                        CommonColors.WHITE,
                        VerticalAlignment.Top,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);
    }

    protected static void renderBackground(PoseStack poseStack, float width, float height) {
        RenderUtils.drawRoundedRectWithBorder(
                poseStack, BORDER_COLOR, FOREGROUND_COLOR, 0, 0, 0, width, height, 2, 6, 8);
    }

    protected abstract void renderConfigAppropriateButton(
            PoseStack poseStack, float width, float height, int mouseX, int mouseY, float partialTicks);

    public abstract void mouseClicked(double mouseX, double mouseY, int button);

    public abstract void keyPressed(int keyCode, int scanCode, int modifiers);

    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
