/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.screens.maps.PoiCreationScreen;
import com.wynntils.screens.maps.PoiManagementScreen;
import com.wynntils.services.map.pois.CustomPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class PoiManagerWidget extends AbstractWidget {
    private final CustomPoi poi;
    private final boolean selected;
    private final boolean selectionMode;
    private final Button editButton;
    private final Button deleteButton;
    private final Button upButton;
    private final Button downButton;
    private final Button selectButton;
    private final float dividedWidth;
    private final int x;
    private final int y;
    private final List<CustomPoi> pois;
    private final PoiManagementScreen managementScreen;

    private CustomColor color;

    public PoiManagerWidget(
            int x,
            int y,
            int width,
            int height,
            CustomPoi poi,
            PoiManagementScreen managementScreen,
            float dividedWidth,
            boolean selectionMode,
            boolean selected) {
        super(x, y, width, height, Component.literal(poi.getName()));
        this.x = x;
        this.y = y;
        this.poi = poi;
        this.managementScreen = managementScreen;
        this.dividedWidth = dividedWidth;
        this.selectionMode = selectionMode;
        this.selected = selected;

        int manageButtonsWidth = (int) (dividedWidth * 4);

        pois = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get();

        color = CommonColors.WHITE;

        if (poi.getVisibility() == CustomPoi.Visibility.HIDDEN) {
            color = CommonColors.GRAY;
        }

        editButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.edit"),
                        (button) -> McUtils.mc().setScreen(PoiCreationScreen.create(managementScreen, poi)))
                .pos(x + (int) (dividedWidth * 28), y)
                .size(manageButtonsWidth, 20)
                .build();

        deleteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.delete"), (button) -> {
                            HiddenConfig<List<CustomPoi>> customPois =
                                    Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;
                            managementScreen.setLastDeletedPoi(
                                    poi, customPois.get().indexOf(poi));
                            customPois.get().remove(poi);
                            customPois.touched();
                            managementScreen.populatePois();
                            managementScreen.createIconFilterButtons();
                        })
                .pos(x + (int) (dividedWidth * 32), y)
                .size(manageButtonsWidth, 20)
                .build();

        upButton = new Button.Builder(Component.literal("ʌ"), (button) -> updateIndex(-1))
                .pos(x + (int) (dividedWidth * 32) + manageButtonsWidth, y)
                .size(10, 20)
                .build();

        downButton = new Button.Builder(Component.literal("v"), (button) -> updateIndex(1))
                .pos(x + (int) (dividedWidth * 32) + manageButtonsWidth + 10, y)
                .size(10, 20)
                .build();

        Component selectButtonText = selected
                ? Component.translatable("screens.wynntils.poiManagementGui.deselect")
                : Component.translatable("screens.wynntils.poiManagementGui.select");

        selectButton = new Button.Builder(selectButtonText, (button) -> managementScreen.selectPoi(poi))
                .pos(x + (int) (dividedWidth * 28), y)
                .size(manageButtonsWidth * 2 + 20, 20)
                .build();

        if (pois.indexOf(poi) == 0) {
            upButton.active = false;
        }

        if (pois.indexOf(poi) == (pois.size() - 1)) {
            downButton.active = false;
        }
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderIcon(poseStack);

        String poiName =
                RenderedStringUtils.getMaxFittingText(poi.getName(), (int) (dividedWidth * 15), McUtils.mc().font);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(poiName),
                        x + (int) (dividedWidth * 3),
                        y + 10,
                        color,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(String.valueOf(poi.getLocation().getX())),
                        x + (int) (dividedWidth * 20),
                        y + 10,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        Optional<Integer> poiY = poi.getLocation().getY();

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        poiY.map(integer -> StyledText.fromString(String.valueOf(integer)))
                                .orElse(StyledText.EMPTY),
                        x + (int) (dividedWidth * 23),
                        y + 10,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(String.valueOf(poi.getLocation().getZ())),
                        x + (int) (dividedWidth * 26),
                        y + 10,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (selectionMode) {
            selectButton.render(poseStack, mouseX, mouseY, partialTick);

            RenderUtils.drawRectBorders(
                    poseStack,
                    selected ? CommonColors.ORANGE : CommonColors.WHITE,
                    x,
                    y + 1,
                    x + width,
                    y + height - 1,
                    0,
                    1f);
        } else {
            editButton.render(poseStack, mouseX, mouseY, partialTick);
            deleteButton.render(poseStack, mouseX, mouseY, partialTick);
            upButton.render(poseStack, mouseX, mouseY, partialTick);
            downButton.render(poseStack, mouseX, mouseY, partialTick);
        }
    }

    private void renderIcon(PoseStack poseStack) {
        float[] poiColor = CustomColor.fromInt(poi.getColor().asInt()).asFloatArray();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(poiColor[0], poiColor[1], poiColor[2], 1);

        RenderUtils.drawTexturedRect(
                poseStack,
                poi.getIcon(),
                x + dividedWidth - (poi.getIcon().width() / 2f),
                y + 10 - (poi.getIcon().height() / 2f));

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private void updateIndex(int direction) {
        int indexToSet = pois.indexOf(poi) + direction;
        pois.remove(poi);
        pois.add(indexToSet, poi);
        managementScreen.populatePois();
        Managers.Config.saveConfig();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clickedButton;

        if (selectionMode) {
            clickedButton = selectButton.mouseClicked(mouseX, mouseY, button);
        } else {
            clickedButton = editButton.mouseClicked(mouseX, mouseY, button)
                    || deleteButton.mouseClicked(mouseX, mouseY, button)
                    || upButton.mouseClicked(mouseX, mouseY, button)
                    || downButton.mouseClicked(mouseX, mouseY, button);
        }

        if (clickedButton) {
            return clickedButton;
        } else {
            managementScreen.selectPoi(poi);
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
