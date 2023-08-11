/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.screens.maps.widgets.PoiManagerWidget;
import com.wynntils.services.map.pois.CustomPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class PoiManagementScreen extends WynntilsScreen {
    private final MainMapScreen oldMapScreen;

    private Button nextButton;
    private Button previousButton;
    private Button undoDeleteButton;

    private List<CustomPoi> waypoints;
    private int pageHeight;
    private int currentPage;
    private final List<CustomPoi> deletedPois = new ArrayList<>();
    private final List<Integer> deletedIndexes = new ArrayList<>();
    private final List<AbstractWidget> poiManagerWidgets = new ArrayList<>();

    private PoiManagementScreen(MainMapScreen oldMapScreen) {
        super(Component.literal("Poi Management Screen"));

        this.oldMapScreen = oldMapScreen;
    }

    public static Screen create() {
        return new PoiManagementScreen(null);
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
                nextButton = new Button.Builder(Component.literal(">"), (button) -> nextPage())
                        .pos(this.width / 2, this.height - 45)
                        .size(20, 20)
                        .build());

        this.addRenderableWidget(
                previousButton = new Button.Builder(Component.literal("<"), (button) -> previousPage())
                        .pos(this.width / 2 - 20, this.height - 45)
                        .size(20, 20)
                        .build());

        this.addRenderableWidget(
                undoDeleteButton = new Button.Builder(
                                Component.translatable("screens.wynntils.poiManagementGui.undo"),
                                (button) -> undoDelete())
                        .pos(
                                this.width
                                        - 25
                                        - font.width(Component.translatable("screens.wynntils.poiManagementGui.undo")),
                                this.height - 45)
                        .size(font.width(Component.translatable("screens.wynntils.poiManagementGui.undo")) + 15, 20)
                        .build());

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.import"),
                        (button) -> importFromClipboard())
                .pos(
                        this.width / 2
                                + 75
                                - font.width(Component.translatable("screens.wynntils.poiManagementGui.import")),
                        this.height - 45)
                .size(font.width(Component.translatable("screens.wynntils.poiManagementGui.import")) + 15, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiManagementGui.import.tooltip")))
                .build());

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.export"),
                        (button) -> exportToClipboard())
                .pos(
                        this.width / 2
                                - 75
                                - font.width(Component.translatable("screens.wynntils.poiManagementGui.export")),
                        this.height - 45)
                .size(font.width(Component.translatable("screens.wynntils.poiManagementGui.export")) + 15, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiManagementGui.export.tooltip")))
                .build());

        if (deletedIndexes.isEmpty()) {
            undoDeleteButton.active = false;
        }

        waypoints = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get();

        if (currentPage * pageHeight > waypoints.size() - 1) {
            currentPage = (waypoints.size() - 1) / pageHeight;
        }

        checkAvailablePages();

        populatePois();
    }

    private void importFromClipboard() {
        String clipboard = McUtils.mc().keyboardHandler.getClipboard();

        CustomPoi[] customPois;
        try {
            customPois = Managers.Json.GSON.fromJson(clipboard, CustomPoi[].class);
        } catch (JsonSyntaxException e) {
            McUtils.sendErrorToClient(I18n.get("screens.wynntils.poiManagementGui.import.error"));
            return;
        }

        if (customPois == null) {
            McUtils.sendErrorToClient(I18n.get("screens.wynntils.poiManagementGui.import.error"));
            return;
        }

        HiddenConfig<List<CustomPoi>> customPoiConfig =
                Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;
        List<CustomPoi> existingPois = customPoiConfig.get();

        List<CustomPoi> poisToAdd = Stream.of(customPois)
                .filter(newPoi -> !existingPois.contains(newPoi))
                .toList();

        existingPois.addAll(poisToAdd);
        customPoiConfig.touched();

        populatePois();

        McUtils.sendMessageToClient(
                Component.translatable("screens.wynntils.poiManagementGui.import.success", poisToAdd.size())
                        .withStyle(ChatFormatting.GREEN));
    }

    private void exportToClipboard() {
        McUtils.mc()
                .keyboardHandler
                .setClipboard(Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois.get().stream()
                        .map(Managers.Json.GSON::toJson)
                        .toList()
                        .toString());

        McUtils.sendMessageToClient(Component.translatable(
                        "screens.wynntils.poiManagementGui.exportedWaypoints",
                        Managers.Feature.getFeatureInstance(MainMapFeature.class)
                                .customPois
                                .get()
                                .size())
                .withStyle(ChatFormatting.GREEN));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.doRender(poseStack, mouseX, mouseY, partialTick);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.poiManagementGui.icon")),
                        this.width / 2f - 165,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.poiManagementGui.name")),
                        this.width / 2f - 130,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString("X"),
                        this.width / 2f - 15,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString("Y"),
                        this.width / 2f + 40,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString("Z"),
                        this.width / 2f + 80,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        RenderUtils.drawRect(poseStack, CommonColors.WHITE, this.width / 2 - 165, 52, 0, 355, 1);
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

        waypoints = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get();

        if (Math.min(pageHeight, waypoints.size() - pageHeight * currentPage) == 0 && currentPage != 0) {
            previousPage();
        }

        for (int i = 0; i < Math.min(pageHeight, waypoints.size() - pageHeight * currentPage); i++) {
            CustomPoi poi = waypoints.get(currentPage * pageHeight + i);

            PoiManagerWidget newWidget = new PoiManagerWidget(0, 0, this.width, this.height, poi, i, this);

            poiManagerWidgets.add(newWidget);

            this.addRenderableWidget(newWidget);
        }

        checkAvailablePages();
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
        Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get()
                .add(deletedIndexes.get(deletedIndexes.size() - 1), deletedPois.get(deletedPois.size() - 1));

        deletedIndexes.remove(deletedIndexes.size() - 1);
        deletedPois.remove(deletedPois.size() - 1);

        if (deletedIndexes.isEmpty()) {
            undoDeleteButton.active = false;
        }

        populatePois();
    }
}
