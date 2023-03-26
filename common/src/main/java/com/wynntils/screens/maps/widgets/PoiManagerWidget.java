/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.features.map.MapFeature;
import com.wynntils.models.map.pois.CustomPoi;
import com.wynntils.screens.maps.PoiCreationScreen;
import com.wynntils.screens.maps.PoiManagementScreen;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

public class PoiManagerWidget extends AbstractWidget {
    private static final List<Texture> POI_ICONS = List.of(
            Texture.FLAG,
            Texture.DIAMOND,
            Texture.FIREBALL,
            Texture.SIGN,
            Texture.STAR,
            Texture.WALL,
            Texture.CHEST_T1,
            Texture.CHEST_T2,
            Texture.CHEST_T3,
            Texture.CHEST_T4,
            Texture.FARMING,
            Texture.FISHING,
            Texture.MINING,
            Texture.WOODCUTTING);

    private CustomPoi poi;
    private Button editButton;
    private Button deleteButton;
    private int row;
    private int colour;
    private int spacingMultiplier = 20; //Small 14, medium 20, large 25
    private static final int ungroupedIndex = Managers.Feature.getFeatureInstance(MapFeature.class)
            .customPois
            .get().size();
    private int group = ungroupedIndex;
    private boolean decreasedSize = (spacingMultiplier == 14);
    private PoiManagementScreen managementScreen;

    public PoiManagerWidget(
            float x, float y, int width, int height, CustomPoi poi, int row, PoiManagementScreen managementScreen) {
        super((int) x, (int) y, width, height, Component.literal(poi.getName()));
        this.poi = poi;
        this.row = row;
        this.managementScreen = managementScreen;

        colour = 0xFFFFFF;

        if (poi.getVisibility() == CustomPoi.Visibility.HIDDEN) {
            colour = 0x636363;
        }

        int groupShift = group == ungroupedIndex ? 20 : 0;

        this.editButton = new Button.Builder(
                Component.translatable("screens.wynntils.poiManagementGui.edit"),
                (button) -> PoiCreationScreen.create(managementScreen, poi))
                .pos(this.width/2 + 85 + groupShift, 54 + spacingMultiplier * row)
                .size((int) Math.round(40.0 * (decreasedSize ? 0.7 : 1.0)), (int) Math.round(20.0 * (decreasedSize ? 0.6 : 1.0)))
                .build();
        this.deleteButton = new Button.Builder(
                Component.translatable("screens.wynntils.poiManagementGui.delete"),
                (button) -> Managers.Feature.getFeatureInstance(MapFeature.class)
                        .customPois
                        .get()
                        .remove(poi))
                .pos(this.width/2 + 130 + groupShift, 54 + spacingMultiplier * row)
                .size((int) Math.round(40.0 * (decreasedSize ? 0.9 : 1.0)), (int) Math.round(20.0 * (decreasedSize ? 0.6 : 1.0)))
                .build();
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderIcon(poseStack, poi, row);

        drawString(poseStack, McUtils.mc().font, StringUtils.abbreviate(poi.getName(), 19), this.width/2 - 130, 60 + spacingMultiplier * row, colour);
        drawCenteredString(poseStack, McUtils.mc().font, String.valueOf(poi.getLocation().getX()), this.width/2 - 15, 60 + spacingMultiplier * row, colour);
        Optional<Integer> y = poi.getLocation().getY();
        drawCenteredString(poseStack, McUtils.mc().font, y.isPresent() ? String.valueOf(y.get()) : "", this.width/2 + 40, 60 + spacingMultiplier * row, colour);
        drawCenteredString(poseStack, McUtils.mc().font, String.valueOf(poi.getLocation().getZ()), this.width/2 + 80, 60 + spacingMultiplier * row, colour);

        editButton.render(poseStack, mouseX, mouseY, partialTick);
        deleteButton.render(poseStack, mouseX, mouseY, partialTick);

        System.out.println("X: " + this.getX() + "\nY: " + this.getY() + "\nWidth: " + this.width + "\nHeight: " + this.height);
    }

    private void renderIcon(PoseStack poseStack, CustomPoi poi, int row) {
        float[] color = CustomColor.fromInt(poi.getColor().asInt()).asFloatArray();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(color[0], color[1], color[2], 1);

        float centreZ = 64 + spacingMultiplier * row;

        RenderUtils.drawTexturedRect(
                poseStack, POI_ICONS.get(POI_ICONS.indexOf(poi.getIcon())), this.width / 2f - 151 - (poi.getIcon().width() / 2f), centreZ - (poi.getIcon().height() / 2f));

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println("Mouse clicked at: " + mouseX + ", " + mouseY);
        System.out.println("Edit button at: " + editButton.getX() + ", " + editButton.getY());
        return editButton.mouseClicked(mouseX, mouseY, button)
                || deleteButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
