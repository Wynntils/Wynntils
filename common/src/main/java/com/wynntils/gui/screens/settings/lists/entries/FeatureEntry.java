/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.lists.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.Feature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.settings.WynntilsSettingsScreen;
import com.wynntils.gui.screens.settings.lists.FeatureList;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import java.util.List;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class FeatureEntry extends FeatureListEntryBase {
    private static final int ITEM_HEIGHT = 25;

    private final Feature feature;
    private final FeatureList featureList;
    private final WynntilsSettingsScreen settingsScreen;

    public FeatureEntry(Feature feature, FeatureList featureList, WynntilsSettingsScreen settingsScreen) {
        this.feature = feature;
        this.featureList = featureList;
        this.settingsScreen = settingsScreen;
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

        renderBackground(poseStack, width, height);

        renderFeatureName(poseStack, width, height);

        renderEnabledSwitch(poseStack);

        poseStack.popPose();
    }

    private void renderEnabledSwitch(PoseStack poseStack) {
        if (!this.feature.canEnable() || !this.feature.canUserEnable()) return;

        float size = getConfigOptionElementSize();

        final Texture switchTexture = this.feature.isEnabled() ? Texture.SWITCH_ON : Texture.SWITCH_OFF;

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                switchTexture.resource(),
                getEnabledSwitchRenderX(),
                getEnabledSwitchRenderY(),
                0,
                size * 2f,
                size,
                switchTexture.width(),
                switchTexture.height());
    }

    private void renderFeatureName(PoseStack poseStack, int width, int height) {
        CustomColor textColor = this.getFeature() == settingsScreen.getSelectedFeature()
                ? CommonColors.LIGHT_GREEN
                : CommonColors.WHITE;
        FontRenderer.getInstance()
                .renderTextWithAlignment(
                        poseStack,
                        9f,
                        0f,
                        new TextRenderTask(
                                this.feature.getTranslatedName(),
                                TextRenderSetting.DEFAULT
                                        .withMaxWidth(getMaxTextRenderWidth())
                                        .withCustomColor(textColor)
                                        .withTextShadow(FontRenderer.TextShadow.OUTLINE)),
                        width,
                        height,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle);
    }

    private void renderBackground(PoseStack poseStack, int width, int height) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.FEATURE_BUTTON.resource(),
                0,
                0,
                0,
                width,
                height,
                Texture.FEATURE_BUTTON.width(),
                Texture.FEATURE_BUTTON.height());
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return ImmutableList.of();
    }

    public float getEnabledSwitchRenderY() {
        return (getRenderHeight() - getConfigOptionElementSize()) / 2f;
    }

    public float getEnabledSwitchRenderX() {
        return settingsScreen.width / 7f - getConfigOptionElementSize();
    }

    public float getConfigOptionElementSize() {
        return ITEM_HEIGHT * 0.5f;
    }

    public Feature getFeature() {
        return feature;
    }

    public static int getItemHeight() {
        return ITEM_HEIGHT;
    }

    private float getMaxTextRenderWidth() {
        return featureList.getRowWidth() - getConfigOptionElementSize() * 2 - 20;
    }

    @Override
    public int getRenderHeight() {
        return (int) FontRenderer.getInstance()
                        .calculateRenderHeight(List.of(this.feature.getTranslatedName()), getMaxTextRenderWidth())
                / FontRenderer.getInstance().getFont().lineHeight
                * FeatureEntry.getItemHeight();
    }
}
