/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.settings.WynntilsSettingsScreen;
import com.wynntils.gui.screens.settings.elements.ConfigOptionElement;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
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
    private final List<ConfigOptionElement> configWidgets = new ArrayList<>();
    private ConfigOptionElement hoveredConfigElement = null;
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

        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.onClick(mouseX, mouseY);

        if (hoveredConfigElement != null) {
            hoveredConfigElement.mouseClicked(mouseX, mouseY, button);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollIndexOffset = MathUtils.clamp(
                scrollIndexOffset - (int) delta * 2, 0, Math.max(0, configWidgets.size() - MAX_RENDER_COUNT));

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //        if (hoveredConfigElement != null) {
        //            hoveredConfigElement.keyPressed(keyCode, scanCode, modifiers);
        //            return true;
        //        }

        return super.keyPressed(keyCode, scanCode, modifiers);
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
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.OUTLINE);
    }

    private void renderConfigWidgets(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        final float padding = settingsScreen.width / 100f;

        float renderY = settingsScreen.getBarHeight() + padding;

        final float xOffset = this.width / 35f;

        hoveredConfigElement = null;

        Overlay lastOverlayConfigTitle = null;

        for (int i = scrollIndexOffset; i < configWidgets.size(); i += 2) {
            ConfigOptionElement configWidgetLeft = configWidgets.get(i);
            ConfigOptionElement configWidgetRight = configWidgets.size() > i + 1 ? configWidgets.get(i + 1) : null;
            float renderHeight = this.height / 4f;

            float fullWidth = this.width - xOffset * 1.5f;
            float renderWidth = fullWidth / 2 - padding;

            if (configWidgetLeft.getConfigHolder().getParent() instanceof Overlay overlay
                    && configWidgetLeft.getConfigHolder().getParent() != lastOverlayConfigTitle) {
                Font font = FontRenderer.getInstance().getFont();
                float textRenderHeight = font.lineHeight + padding;

                if (renderY + textRenderHeight > this.y + this.height) break;

                lastOverlayConfigTitle = overlay;

                FontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
                                overlay.getTranslatedName(),
                                0,
                                this.width,
                                renderY,
                                0,
                                CommonColors.WHITE,
                                HorizontalAlignment.Center,
                                FontRenderer.TextShadow.OUTLINE);
                renderY += textRenderHeight;
            }

            if (renderY + renderHeight > this.y + this.height) break;

            //            configWidgetLeft.render(
            //                    poseStack, xOffset, renderY, renderWidth, renderHeight, mouseX, mouseY, partialTick);

            float actualRenderX = this.x + xOffset;
            float actualRenderY = this.y + renderY;

            if (isMouseOverConfigWidget(actualRenderX, actualRenderY, renderWidth, renderHeight, mouseX, mouseY)) {
                this.hoveredConfigElement = configWidgetLeft;
            }

            if (configWidgetRight != null) {
                actualRenderX = this.x + xOffset + fullWidth / 2;
                renderWidth = fullWidth / 2;
                float renderX = xOffset + fullWidth / 2;

                if (isMouseOverConfigWidget(actualRenderX, actualRenderY, renderWidth, renderHeight, mouseX, mouseY)) {
                    this.hoveredConfigElement = configWidgetRight;
                }

                if (configWidgetRight.getConfigHolder().getParent() instanceof Overlay
                        && configWidgetRight.getConfigHolder().getParent() != lastOverlayConfigTitle) {
                    renderY += renderHeight + padding;

                    // render current right element on left
                    i--;
                    continue;
                }
                //
                //                configWidgetRight.render(
                //                        poseStack, renderX, renderY, renderWidth, renderHeight, mouseX, mouseY,
                // partialTick);
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

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.SCROLL_BUTTON.resource(),
                xPos,
                yPos,
                0,
                size,
                size,
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
        hoveredConfigElement = null;

        if (settingsScreen.getFocusedTextInput() != settingsScreen.getSearchWidget()) {
            settingsScreen.setFocusedTextInput(null);
        }

        for (ConfigHolder configOption : selectedFeature.getVisibleConfigOptions()) {
            if (configOption.getFieldName().equals("userEnabled")) {
                continue;
            }

            getWidgetFromConfigHolder(configOption);
        }

        for (Overlay overlay : selectedFeature.getOverlays()) {
            for (ConfigHolder configOption : overlay.getVisibleConfigOptions()) {
                if (configOption.getFieldName().equals("userEnabled")) {
                    continue;
                }

                getWidgetFromConfigHolder(configOption);
            }
        }

        cachedFeature = selectedFeature;
    }

    private void getWidgetFromConfigHolder(ConfigHolder configOption) {
        //        if (configOption.getType().equals(Boolean.class)) {
        //            configWidgets.add(new BooleanConfigOptionElement(configOption, this, settingsScreen));
        //        } else if (configOption.getClassOfConfigField().isEnum()) {
        //            configWidgets.add(new EnumConfigOptionElement(configOption, this, settingsScreen));
        //        } else if (configOption.getType().equals(CustomColor.class)) {
        //            configWidgets.add(new CustomColorConfigOptionElement(configOption, this, settingsScreen));
        //        } else {
        //            configWidgets.add(new TextConfigOptionElement(configOption, this, settingsScreen));
        //        }
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

    public ConfigOptionElement getHoveredConfigElement() {
        return hoveredConfigElement;
    }
}
