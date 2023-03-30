/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.features.map.MapFeature;
import com.wynntils.models.map.pois.CustomPoi;
import com.wynntils.screens.base.WynntilsScreen;
import com.wynntils.screens.maps.widgets.PoiManagerWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class PoiManagementScreen extends WynntilsScreen {
    private final MainMapScreen oldMapScreen;
    private Button nextButton;
    private Button previousButton;
    private Button undoDeleteButton;
    private List<CustomPoi> waypoints;
    private int pageHeight;
    private int currentPage;
    private List<CustomPoi> deletedPois = new ArrayList<>();
    private List<Integer> deletedIndexes = new ArrayList<>();
    private final List<AbstractWidget> poiManagerWidgets = new ArrayList<>();

    private PoiManagementScreen(MainMapScreen oldMapScreen) {
        super(Component.literal("Poi Management Screen"));

        this.oldMapScreen = oldMapScreen;
    }

    public static Screen create(MainMapScreen oldMapScreen) {
        return new PoiManagementScreen(oldMapScreen);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(oldMapScreen);
    }

    @Override
    protected void doInit() {
        pageHeight = (this.height - 100) / 20;

        this.addRenderableWidget(
                new Button.Builder(Component.literal("X").withStyle(ChatFormatting.RED), (button) -> this.onClose())
                        .pos(this.width - 40, 20)
                        .size(20, 20)
                        .build());

        this.addRenderableWidget(
                nextButton = new Button.Builder(Component.literal(">"), (button) -> {
                            nextPage();
                        })
                        .pos(this.width / 2 + 2, this.height - 45)
                        .size(20, 20)
                        .build());

        this.addRenderableWidget(
                previousButton = new Button.Builder(Component.literal("<"), (button) -> {
                            previousPage();
                        })
                        .pos(this.width / 2 - 22, this.height - 45)
                        .size(20, 20)
                        .build());

        this.addRenderableWidget(
                undoDeleteButton = new Button.Builder(
                                Component.translatable("screens.wynntils.poiManagementGui.undo"), (button) -> {
                                    undoDelete();
                                })
                        .pos(
                                this.width
                                        - 25
                                        - font.width(Component.translatable("screens.wynntils.poiManagementGui.undo")),
                                this.height - 45)
                        .size(font.width(Component.translatable("screens.wynntils.poiManagementGui.undo")) + 15, 20)
                        .build());

        if (deletedIndexes.isEmpty()) {
            undoDeleteButton.active = false;
        }

        waypoints =
                Managers.Feature.getFeatureInstance(MapFeature.class).customPois.get();

        if (currentPage * pageHeight > waypoints.size() - 1) {
            currentPage = (waypoints.size() - 1) / pageHeight;
        }

        checkAvailablePages();

        populatePois();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.doRender(poseStack, mouseX, mouseY, partialTick);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.poiManagementGui.icon"),
                        this.width / 2f - 165,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.poiManagementGui.name"),
                        this.width / 2f - 130,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        "X",
                        this.width / 2f - 15,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        "Y",
                        this.width / 2f + 40,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        "Z",
                        this.width / 2f + 80,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        RenderUtils.drawRect(poseStack, CommonColors.WHITE, this.width / 2 - 165, 52, 0, 355, 1);

        poiManagerWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta < 0.0 && nextButton.active) {
            nextPage();
        } else if (delta > 0.0 && previousButton.active) {
            previousPage();
        }

        return true;
    }

    public void populatePois() {
        for (AbstractWidget widget : poiManagerWidgets) {
            this.removeWidget(widget);
        }

        this.poiManagerWidgets.clear();

        waypoints =
                Managers.Feature.getFeatureInstance(MapFeature.class).customPois.get();

        if (Math.min(pageHeight, waypoints.size() - pageHeight * currentPage) == 0 && currentPage != 0) {
            previousPage();
        }

        for (int i = 0; i < Math.min(pageHeight, waypoints.size() - pageHeight * currentPage); i++) {
            CustomPoi poi = waypoints.get(currentPage * pageHeight + i);

            PoiManagerWidget newWidget = new PoiManagerWidget(0, 0, this.width, this.height, poi, i, this);

            poiManagerWidgets.add(newWidget);

            this.addRenderableWidget(newWidget);
        }
    }

    private void nextPage() {
        currentPage++;
        checkAvailablePages();
        populatePois();
    }

    private void previousPage() {
        currentPage--;
        checkAvailablePages();
        populatePois();
    }

    private void checkAvailablePages() {
        nextButton.active = waypoints.size() - currentPage * pageHeight > pageHeight;
        previousButton.active = currentPage > 0;
    }

    public void setLastDeletedPoi(CustomPoi deletedPoi, int deletedPoiIndex) {
        deletedPois.add(deletedPoi);
        deletedIndexes.add(deletedPoiIndex);

        undoDeleteButton.active = true;
    }

    private void undoDelete() {
        Managers.Feature.getFeatureInstance(MapFeature.class).customPois.get().add(deletedIndexes.get(deletedIndexes.size() - 1), deletedPois.get(deletedPois.size() - 1));

        deletedIndexes.remove(deletedIndexes.size() - 1);
        deletedPois.remove(deletedPois.size() - 1);

        if (deletedIndexes.isEmpty()) {
            undoDeleteButton.active = false;
        }

        populatePois();
    }
}
