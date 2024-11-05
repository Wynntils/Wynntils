/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.impl.MapVisibilityImpl;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.builtin.WaypointLocation;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class WaypointVisibilityScreen extends AbstractMapScreen {
    // Constants
    private static final float GRID_DIVISIONS = 64.0f;

    // Collections
    private final List<VisibilitySlider> visibilitySliders = new ArrayList<>();

    // Widgets
    private VisibilitySlider minVisibilitySlider;
    private VisibilitySlider maxVisibilitySlider;
    private VisibilitySlider fadeSlider;

    // UI Size, positions etc
    private float dividedWidth;
    private float dividedHeight;

    // Screen information
    private final WaypointCreationScreen previousScreen;
    private final boolean editinglabel;
    private boolean firstSetup = true;

    // Details
    private WaypointLocation waypointPreview;

    private WaypointVisibilityScreen(
            WaypointCreationScreen previousScreen, WaypointLocation waypointPreview, boolean label) {
        this.previousScreen = previousScreen;
        this.waypointPreview = waypointPreview;
        this.editinglabel = label;
    }

    public static Screen create(WaypointCreationScreen returnScreen, WaypointLocation featurePreview, boolean label) {
        return new WaypointVisibilityScreen(returnScreen, featurePreview, label);
    }

    @Override
    protected void doInit() {
        dividedWidth = this.width / GRID_DIVISIONS;
        dividedHeight = this.height / GRID_DIVISIONS;

        renderX = dividedWidth * 32;
        renderWidth = dividedWidth * 29;
        renderY = dividedHeight * 5;
        renderHeight = dividedHeight * 54;

        float borderScaleX = (float) this.width / Texture.FULLSCREEN_MAP_BORDER.width();
        float borderScaleY = (float) this.height / Texture.FULLSCREEN_MAP_BORDER.height();

        renderedBorderXOffset = 3 * borderScaleX;
        renderedBorderYOffset = 3 * borderScaleY;

        mapWidth = renderWidth - renderedBorderXOffset * 2f + 1; // +1 to fix rounding causing black line on the right
        centerX = renderX + renderedBorderXOffset + mapWidth / 2f;
        mapHeight = renderHeight - renderedBorderYOffset * 2f;
        centerZ = renderY + renderedBorderYOffset + mapHeight / 2f;

        visibilitySliders.clear();

        float min;
        float max;
        float fade;

        if (firstSetup) {
            updateMapCenter(
                    waypointPreview.getLocation().x(),
                    waypointPreview.getLocation().z());

            ResolvedMapAttributes attributes = Services.MapData.resolveMapAttributes(waypointPreview);

            if (editinglabel) {
                min = attributes.labelVisibility().min();
                max = attributes.labelVisibility().max();
                fade = attributes.labelVisibility().fade();
            } else {
                min = attributes.iconVisibility().min();
                max = attributes.iconVisibility().max();
                fade = attributes.iconVisibility().fade();
            }
        } else {
            min = minVisibilitySlider.getVisibility();
            max = maxVisibilitySlider.getVisibility();
            fade = fadeSlider.getVisibility();
        }

        minVisibilitySlider = new VisibilitySlider(
                (int) dividedWidth,
                (int) (dividedHeight * 17),
                (int) (dividedWidth * 9),
                Component.literal(String.valueOf((int) min)),
                min / 100,
                0.01);
        visibilitySliders.add(minVisibilitySlider);
        this.addRenderableWidget(minVisibilitySlider);

        maxVisibilitySlider = new VisibilitySlider(
                (int) (dividedWidth * 11),
                (int) (dividedHeight * 17),
                (int) (dividedWidth * 9),
                Component.literal(String.valueOf((int) max)),
                max / 100,
                0.01);
        visibilitySliders.add(maxVisibilitySlider);
        this.addRenderableWidget(maxVisibilitySlider);

        fadeSlider = new VisibilitySlider(
                (int) (dividedWidth * 21),
                (int) (dividedHeight * 17),
                (int) (dividedWidth * 9),
                Component.literal(String.valueOf((int) fade)),
                fade / 100,
                0.0);
        visibilitySliders.add(fadeSlider);
        this.addRenderableWidget(fadeSlider);

        updateSliders(min, max, fade);

        // region presets
        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.waypointVisibility.always"), (button) -> {
                            float alwaysMin = editinglabel
                                    ? DefaultMapAttributes.LABEL_ALWAYS.getMin().get()
                                    : DefaultMapAttributes.ICON_ALWAYS.getMin().get();
                            float alwaysMax = editinglabel
                                    ? DefaultMapAttributes.LABEL_ALWAYS.getMax().get()
                                    : DefaultMapAttributes.ICON_ALWAYS.getMax().get();
                            float alwaysFade = editinglabel
                                    ? DefaultMapAttributes.LABEL_ALWAYS
                                            .getFade()
                                            .get()
                                    : DefaultMapAttributes.ICON_ALWAYS.getFade().get();

                            updateSliders(alwaysMin, alwaysMax, alwaysFade);
                        })
                        .pos((int) (dividedWidth * 4), (int) (dividedHeight * 23))
                        .size((int) (dividedWidth * 9), 20)
                        .build());

        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.waypointVisibility.never"), (button) -> {
                            float neverMin = editinglabel
                                    ? DefaultMapAttributes.LABEL_NEVER.getMin().get()
                                    : DefaultMapAttributes.ICON_NEVER.getMin().get();
                            float neverMax = editinglabel
                                    ? DefaultMapAttributes.LABEL_NEVER.getMax().get()
                                    : DefaultMapAttributes.ICON_NEVER.getMax().get();
                            float neverFade = editinglabel
                                    ? DefaultMapAttributes.LABEL_NEVER.getFade().get()
                                    : DefaultMapAttributes.ICON_NEVER.getFade().get();

                            updateSliders(neverMin, neverMax, neverFade);
                        })
                        .pos((int) (dividedWidth * 18), (int) (dividedHeight * 23))
                        .size((int) (dividedWidth * 9), 20)
                        .build());
        // endregion

        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.waypointVisibility.close"), (button) -> {
                            this.onClose();
                        })
                        .pos((int) (dividedWidth * 11), (int) (dividedHeight * 48))
                        .size((int) (dividedWidth * 9), 20)
                        .build());

        firstSetup = false;
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderGradientBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();
        renderMap(poseStack);
        RenderUtils.enableScissor(
                (int) (renderX + renderedBorderXOffset), (int) (renderY + renderedBorderYOffset), (int) mapWidth, (int)
                        mapHeight);

        renderMapFeatures(poseStack, mouseX, mouseY);

        RenderUtils.disableScissor();

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable(
                                editinglabel
                                        ? "screens.wynntils.waypointVisibility.labelVisibility"
                                        : "screens.wynntils.waypointVisibility.iconVisibility")),
                        dividedWidth * 15.5f,
                        dividedHeight * 8,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        2f);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.waypointVisibility.minVisibility") + ":"),
                        dividedWidth,
                        dividedHeight * 15.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.waypointVisibility.maxVisibility") + ":"),
                        dividedWidth * 11.0f,
                        dividedHeight * 15.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.waypointVisibility.fade") + ":"),
                        dividedWidth * 21.0f,
                        dividedHeight * 15.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.waypointVisibility.description1")),
                        dividedWidth * 2.0f,
                        dividedWidth * 29.0f,
                        dividedHeight * 28f,
                        dividedHeight * 32f,
                        dividedWidth * 27f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.waypointVisibility.description2")),
                        dividedWidth * 2.0f,
                        dividedWidth * 29.0f,
                        dividedHeight * 34f,
                        dividedHeight * 38f,
                        dividedWidth * 27f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.waypointVisibility.description3")),
                        dividedWidth * 2.0f,
                        dividedWidth * 29.0f,
                        dividedHeight * 40f,
                        dividedHeight * 44f,
                        dividedWidth * 27f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        renderZoomWidget(poseStack);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(previousScreen);
    }

    @Override
    protected Stream<MapFeature> getRenderedMapFeatures() {
        return Stream.of(waypointPreview);
    }

    private void updateVisibility() {
        MapVisibilityImpl visibility = new MapVisibilityImpl(
                (float) minVisibilitySlider.getVisibility(), (float) maxVisibilitySlider.getVisibility(), (float)
                        fadeSlider.getVisibility());

        previousScreen.updateVisibility(visibility, editinglabel);

        waypointPreview = previousScreen.getWaypoint();
    }

    private void updateSliders(float min, float max, float fade) {
        minVisibilitySlider.setVisibility(min);
        maxVisibilitySlider.setVisibility(max);
        fadeSlider.setVisibility(fade);
    }

    private final class VisibilitySlider extends AbstractSliderButton {
        private static final int BUTTON_HEIGHT = 20;
        private final double min;

        private VisibilitySlider(int x, int y, int width, Component message, double value, double min) {
            super(x, y, width, BUTTON_HEIGHT, message, value);
            this.min = min;
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(String.valueOf(getVisibility())));
        }

        @Override
        protected void applyValue() {
            this.value = Mth.clamp((double) getVisibility() / 100, min, 1.0);
            updateVisibility();
        }

        public int getVisibility() {
            return (int) (value * 100);
        }

        public void setVisibility(Float visibility) {
            this.value = Mth.clamp((double) visibility / 100, min, 1.0);

            updateMessage();
            updateVisibility();
        }
    }
}
