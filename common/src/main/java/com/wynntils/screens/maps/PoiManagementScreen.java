/*
 * Copyright © Wynntils 2022.
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
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PoiManagementScreen extends WynntilsScreen {
    private final MainMapScreen oldMapScreen;
    private Button nextButton;
    private Button previousButton;
    private List<CustomPoi> waypoints;
    private int pageHeight;
    private int page;
    private int spacingMultiplier = 20; //Small 14, medium 20, large 25. Make customisable
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
        super.doInit();

        pageHeight = (this.height - 100) / spacingMultiplier;

        this.addRenderableWidget(new Button.Builder(
                Component.translatable("screens.wynntils.poiManagementGui.close").withStyle(ChatFormatting.RED), (button) -> this.onClose())
                .pos(this.width - 40, 20)
                .size(20, 20)
                .build());

        this.addRenderableWidget(
                nextButton = new Button.Builder(
                                Component.translatable("screens.wynntils.poiManagementGui.next"), (button) -> {
                                    nextPage();
                                })
                        .pos(this.width/2 + 2, this.height - 45)
                        .size(20, 20)
                        .build());

        this.addRenderableWidget(
                previousButton = new Button.Builder(
                                Component.translatable("screens.wynntils.poiManagementGui.previous"), (button) -> {
                                    previousPage();
                                })
                        .pos(this.width/2 - 22, this.height - 45)
                        .size(20, 20)
                        .build());

        onWaypointChange();
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
                        I18n.get("screens.wynntils.poiManagementGui.x"),
                        this.width / 2f - 15,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.poiManagementGui.y"),
                        this.width / 2f + 40,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.poiManagementGui.z"),
                        this.width / 2f + 80,
                        43,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        waypoints = Managers.Feature.getFeatureInstance(MapFeature.class)
                .customPois
                .get();

        poiManagerWidgets.clear();

        for (int i = 0, lim = Math.min(pageHeight, waypoints.size() - pageHeight * page); i < lim; i++) {
            CustomPoi poi = waypoints.get(page * pageHeight + i);

            if (poi == null) {
                continue;
            }

            //Needs values changing. Possibly the source of the edit/delete buttons not registering interactions.
            poiManagerWidgets.add(new PoiManagerWidget(
                    0,
                    0,
                    this.width,
                    this.height,
                    poi,
                    i,
                    this));
        }

        poiManagerWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
    }

    private void onWaypointChange() {
        waypoints = Managers.Feature.getFeatureInstance(MapFeature.class)
                .customPois
                .get();

        if (!waypoints.isEmpty() && page * pageHeight > waypoints.size() - 1) {
            page = (waypoints.size() - 1) / pageHeight;
        }

        checkAvailablePages();
    }

    private void nextPage() {
        page++;
        checkAvailablePages();
    }

    private void previousPage() {
        page--;
        checkAvailablePages();
    }

    private void checkAvailablePages() {
        nextButton.active = waypoints.size() - page * pageHeight > pageHeight;
        previousButton.active = page > 0;
    }
}
