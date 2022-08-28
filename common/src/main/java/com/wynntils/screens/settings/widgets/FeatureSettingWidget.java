/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.screens.settings.ConfigOptionElement;
import com.wynntils.screens.settings.WynntilsSettingsScreen;
import com.wynntils.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public final class FeatureSettingWidget extends AbstractWidget {
    private static final float PADDING = 5.0f;

    private static final CustomColor BORDER_COLOR = new CustomColor(86, 75, 61, 255);
    private static final CustomColor FOREGROUND_COLOR = new CustomColor(177, 152, 120, 255);
    private static final CustomColor SCROLLBAR_COLOR = new CustomColor(137, 117, 92, 255);

    private final WynntilsSettingsScreen settingsScreen;

    private Feature cachedFeature = null;
    private List<ConfigOptionElement> configWidgets = new ArrayList<>();
    private int scrollIndexOffset = 0;

    public FeatureSettingWidget(int x, int y, int width, int height, WynntilsSettingsScreen settingsScreen) {
        super(x, y, width, height, new TextComponent("Feature Setting Widget"));

        this.settingsScreen = settingsScreen;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        recalculateConfigOptions();

        poseStack.pushPose();
        poseStack.translate(this.x, this.y, 0);

        renderBackground(poseStack);
        renderScrollbar(poseStack);

        Feature selectedFeature = settingsScreen.getSelectedFeature();
        if (selectedFeature == null) return;

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        selectedFeature.getTranslatedName(),
                        0,
                        this.width,
                        8,
                        this.width,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);

        renderConfigWidgets(poseStack, mouseX, mouseY, partialTick);

        poseStack.popPose();
    }

    private void renderConfigWidgets(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        float renderY = settingsScreen.getBarHeight() + PADDING;

        final float xOffset = this.width / 35f;

        for (int i = scrollIndexOffset; i < configWidgets.size(); i++) {
            ConfigOptionElement configWidget = configWidgets.get(i);
            float renderHeight = this.height / 4f;

            if (renderY + renderHeight > this.y + this.height) break;

            configWidget.render(
                    poseStack,
                    xOffset,
                    renderY,
                    this.width - xOffset * 1.5f,
                    renderHeight,
                    mouseX,
                    mouseY,
                    partialTick);

            renderY += PADDING + renderHeight;
        }
    }

    private void renderScrollbar(PoseStack poseStack) {
        final float biggerWidth = this.width / 70f;
        final float smallerWidth = this.width / 140f;

        RenderUtils.drawRect(poseStack, SCROLLBAR_COLOR, 10, 9, 0, biggerWidth, this.height - 18);

        float offset = (biggerWidth - smallerWidth) / 2;

        RenderUtils.drawRect(poseStack, SCROLLBAR_COLOR, 10 + offset, 6, 0, smallerWidth, this.height - 12);
    }

    private void renderBackground(PoseStack poseStack) {
        RenderUtils.drawRoundedRectWithBorder(
                poseStack, BORDER_COLOR, FOREGROUND_COLOR, 2, 2, 0, this.width - 4, this.height - 4, 2, 6, 8);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.onClick(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollIndexOffset = MathUtils.clamp(scrollIndexOffset - (int) delta, 0, Math.max(0, configWidgets.size() - 3));

        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    private void recalculateConfigOptions() {
        Feature selectedFeature = settingsScreen.getSelectedFeature();

        if (selectedFeature == cachedFeature) return;

        configWidgets.clear();
        scrollIndexOffset = 0;

        for (ConfigHolder configOption : selectedFeature.getVisibleConfigOptions()) {
            configWidgets.add(new ConfigOptionElement(configOption));
        }

        cachedFeature = selectedFeature;
    }
}
