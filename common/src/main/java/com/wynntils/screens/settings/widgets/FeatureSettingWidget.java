/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.screens.settings.WynntilsSettingsScreen;
import com.wynntils.screens.settings.elements.BooleanConfigOptionElement;
import com.wynntils.screens.settings.elements.ConfigOptionElement;
import com.wynntils.screens.settings.elements.DummyConfigOptionElement;
import com.wynntils.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public final class FeatureSettingWidget extends AbstractWidget {
    private static final int MAX_RENDER_COUNT = 6;

    private static final CustomColor BORDER_COLOR = new CustomColor(86, 75, 61, 255);
    private static final CustomColor FOREGROUND_COLOR = new CustomColor(177, 152, 120, 255);
    private static final CustomColor SCROLLBAR_COLOR = new CustomColor(137, 117, 92, 255);

    private final WynntilsSettingsScreen settingsScreen;

    private Feature cachedFeature = null;
    private List<ConfigOptionElement> configWidgets = new ArrayList<>();
    private ConfigOptionElement hoveredConfigWidget = null;
    private boolean enabledStateChangeable = false;
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

        Feature selectedFeature = settingsScreen.getSelectedFeature();
        if (selectedFeature == null) return;

        renderTitle(poseStack, selectedFeature);

        renderConfigWidgets(poseStack, mouseX, mouseY, partialTick);

        renderScrollbar(poseStack);

        renderEnabledSwitch(poseStack, selectedFeature);

        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.onClick(mouseX, mouseY);

        if (hoveredConfigWidget != null) {
            hoveredConfigWidget.mouseClicked(mouseX, mouseY, button);
            return true;
        }

        float switchRenderX = getEnabledSwitchRenderX() + this.x;
        float switchRenderY = getEnabledSwitchRenderY() + this.y;
        float switchSize = getEnabledSwitchSize();

        // Clicked on switch
        if (mouseX >= switchRenderX
                && mouseX <= switchRenderX + switchSize * 2
                && mouseY >= switchRenderY
                && mouseY <= switchRenderY + switchSize) {
            if (!(cachedFeature instanceof UserFeature userFeature)) {
                WynntilsMod.error(cachedFeature + " had userEnabled field, but is not a UserFeature.");
                assert false;
                return false;
            }

            userFeature.setUserEnabled(!userFeature.isEnabled());
            userFeature.tryUserToggle();

            return true;
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollIndexOffset = MathUtils.clamp(
                scrollIndexOffset - (int) delta * 2, 0, Math.max(0, configWidgets.size() - MAX_RENDER_COUNT));

        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    private void renderTitle(PoseStack poseStack, Feature selectedFeature) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        selectedFeature.getTranslatedName(),
                        0,
                        this.width,
                        this.height / 50f,
                        this.width,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);
    }

    private void renderEnabledSwitch(PoseStack poseStack, Feature selectedFeature) {
        if (!enabledStateChangeable) return;

        float size = getEnabledSwitchSize();

        final Texture switchTexture = selectedFeature.isEnabled() ? Texture.SWITCH_ON : Texture.SWITCH_OFF;

        RenderUtils.drawTexturedRect(
                poseStack,
                switchTexture.resource(),
                getEnabledSwitchRenderX(),
                getEnabledSwitchRenderY(),
                0,
                size * 2f,
                size,
                0,
                0,
                switchTexture.width(),
                switchTexture.height(),
                switchTexture.width(),
                switchTexture.height());
    }

    private void renderConfigWidgets(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        final float padding = settingsScreen.width / 100f;

        float renderY = settingsScreen.getBarHeight() + padding;

        final float xOffset = this.width / 35f;

        hoveredConfigWidget = null;

        for (int i = scrollIndexOffset; i < configWidgets.size(); i += 2) {
            ConfigOptionElement configWidgetLeft = configWidgets.get(i);
            ConfigOptionElement configWidgetRight = configWidgets.size() > i + 1 ? configWidgets.get(i + 1) : null;
            float renderHeight = this.height / 4f;

            if (renderY + renderHeight > this.y + this.height) break;

            float fullWidth = this.width - xOffset * 1.5f;
            float renderWidth = fullWidth / 2 - padding;
            configWidgetLeft.render(
                    poseStack, xOffset, renderY, renderWidth, renderHeight, mouseX, mouseY, partialTick);

            float actualRenderX = this.x + xOffset;
            float actualRenderY = this.y + renderY;

            if (isMouseOverConfigWidget(actualRenderX, actualRenderY, renderWidth, renderHeight, mouseX, mouseY)) {
                this.hoveredConfigWidget = configWidgetLeft;
            }

            if (configWidgetRight != null) {
                actualRenderX = this.x + xOffset + fullWidth / 2;
                renderWidth = fullWidth / 2;

                if (isMouseOverConfigWidget(actualRenderX, actualRenderY, renderWidth, renderHeight, mouseX, mouseY)) {
                    this.hoveredConfigWidget = configWidgetRight;
                }

                configWidgetRight.render(
                        poseStack,
                        xOffset + fullWidth / 2,
                        renderY,
                        renderWidth,
                        renderHeight,
                        mouseX,
                        mouseY,
                        partialTick);
            }

            renderY += padding + renderHeight;
        }
    }

    private void renderScrollbar(PoseStack poseStack) {
        if (configWidgets.size() <= MAX_RENDER_COUNT) return;

        final float biggerWidth = this.width / 70f;
        final float smallerWidth = this.width / 140f;

        final float renderX = settingsScreen.width / 160f;

        RenderUtils.drawRect(poseStack, SCROLLBAR_COLOR, renderX, 9, 0, biggerWidth, this.height - 18);

        float offset = (biggerWidth - smallerWidth) / 2;

        RenderUtils.drawRect(poseStack, SCROLLBAR_COLOR, renderX + offset, 6, 0, smallerWidth, this.height - 12);

        int size = (int) (settingsScreen.width / 65f);

        float xPos = renderX / 1.45f;
        float yPos = getScrollButtonYPos();

        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.SCROLL_BUTTON.resource(),
                xPos,
                yPos,
                0,
                size,
                size,
                0,
                0,
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height(),
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height());
    }

    private void renderBackground(PoseStack poseStack) {
        RenderUtils.drawRoundedRectWithBorder(
                poseStack, BORDER_COLOR, FOREGROUND_COLOR, 2, 2, 0, this.width - 4, this.height - 4, 2, 6, 8);
    }

    private boolean isMouseOverConfigWidget(
            float actualRenderX, float actualRenderY, float renderWidth, float renderHeight, int mouseX, int mouseY) {
        return mouseX >= actualRenderX
                && mouseX <= actualRenderX + renderWidth
                && mouseY >= actualRenderY
                && mouseY < actualRenderY + renderHeight;
    }

    private void recalculateConfigOptions() {
        Feature selectedFeature = settingsScreen.getSelectedFeature();

        if (selectedFeature == cachedFeature) return;

        configWidgets.clear();
        scrollIndexOffset = 0;
        enabledStateChangeable = false;
        hoveredConfigWidget = null;

        for (ConfigHolder configOption : selectedFeature.getVisibleConfigOptions()) {
            if (configOption.getFieldName().equals("userEnabled")) {
                enabledStateChangeable = true;
                continue;
            }

            if (configOption.getType().equals(Boolean.class)) {
                configWidgets.add(new BooleanConfigOptionElement(configOption, this));
            } else {
                configWidgets.add(new DummyConfigOptionElement(configOption, this));
            }
        }

        cachedFeature = selectedFeature;
    }

    private float getRenderHeight() {
        return settingsScreen.height - settingsScreen.getBarHeight() * 2;
    }

    private float getScrollButtonYPos() {
        float height = getRenderHeight();

        return MathUtils.map(
                this.scrollIndexOffset,
                0,
                Math.max(0, configWidgets.size() - MAX_RENDER_COUNT),
                5,
                height - this.y - 2);
    }

    private float getEnabledSwitchRenderY() {
        return this.height / 25f;
    }

    private float getEnabledSwitchRenderX() {
        return this.width / 2f - getEnabledSwitchSize();
    }

    public float getEnabledSwitchSize() {
        return this.width / 80f;
    }

    public ConfigOptionElement getHoveredConfigWidget() {
        return hoveredConfigWidget;
    }
}
