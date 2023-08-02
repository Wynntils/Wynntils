/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.HiddenConfig;
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
    private final Button editButton;
    private final Button deleteButton;
    private final Button upButton;
    private final Button downButton;
    private final int row;
    private final PoiManagementScreen managementScreen;
    private final List<CustomPoi> pois;

    private CustomColor color;

    public PoiManagerWidget(
            float x, float y, int width, int height, CustomPoi poi, int row, PoiManagementScreen managementScreen) {
        super((int) x, (int) y, width, height, Component.literal(poi.getName()));
        this.poi = poi;
        this.row = row;
        this.managementScreen = managementScreen;

        pois = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get();

        color = CommonColors.WHITE;

        if (poi.getVisibility() == CustomPoi.Visibility.HIDDEN) {
            color = CommonColors.GRAY;
        }

        this.editButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.edit"),
                        (button) -> McUtils.mc().setScreen(PoiCreationScreen.create(managementScreen, poi)))
                .pos(this.width / 2 + 85 + 20, 54 + 20 * row)
                .size(40, 20)
                .build();

        this.deleteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.delete"), (button) -> {
                            HiddenConfig<List<CustomPoi>> customPois =
                                    Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;
                            managementScreen.setLastDeletedPoi(
                                    poi, customPois.get().indexOf(poi));
                            customPois.get().remove(poi);
                            customPois.touched();
                            managementScreen.populatePois();
                        })
                .pos(this.width / 2 + 130 + 20, 54 + 20 * row)
                .size(40, 20)
                .build();

        this.upButton = new Button.Builder(Component.literal("\u2303"), (button) -> updateIndex(-1))
                .pos(this.width / 2 + 172 + 20, 54 + 20 * row)
                .size(9, 9)
                .build();

        this.downButton = new Button.Builder(Component.literal("\u2304"), (button) -> updateIndex(1))
                .pos(this.width / 2 + 172 + 20, 54 + 20 * row + 9)
                .size(9, 9)
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

        int maxTextWidth = 90;
        String poiName = RenderedStringUtils.getMaxFittingText(poi.getName(), maxTextWidth, McUtils.mc().font);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(poiName),
                        this.width / 2f - 130,
                        60 + 20 * row,
                        color,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(String.valueOf(poi.getLocation().getX())),
                        this.width / 2f - 15,
                        60 + 20 * row,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        Optional<Integer> y = poi.getLocation().getY();

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        y.isPresent() ? StyledText.fromString(String.valueOf(y.get())) : StyledText.EMPTY,
                        this.width / 2f + 40,
                        60 + 20 * row,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(String.valueOf(poi.getLocation().getZ())),
                        this.width / 2f + 80,
                        60 + 20 * row,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        editButton.render(poseStack, mouseX, mouseY, partialTick);
        deleteButton.render(poseStack, mouseX, mouseY, partialTick);
        upButton.render(poseStack, mouseX, mouseY, partialTick);
        downButton.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderIcon(PoseStack poseStack) {
        float[] poiColor = CustomColor.fromInt(poi.getColor().asInt()).asFloatArray();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(poiColor[0], poiColor[1], poiColor[2], 1);

        float centreZ = 64 + 20 * row;

        RenderUtils.drawTexturedRect(
                poseStack,
                poi.getIcon(),
                this.width / 2f - 151 - (poi.getIcon().width() / 2f),
                centreZ - (poi.getIcon().height() / 2f));

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
        return editButton.mouseClicked(mouseX, mouseY, button)
                || deleteButton.mouseClicked(mouseX, mouseY, button)
                || upButton.mouseClicked(mouseX, mouseY, button)
                || downButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
