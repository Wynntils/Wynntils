/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.type.OverrideType;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.type.MapArea;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.features.type.MapLocation;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;

public class FeatureEntryWidget extends AbstractWidget implements TooltipProvider {
    private static final int PADDING = 4;
    private static final int ICON_BORDER = 2;
    private static final int TEXT_GAP = 6;

    private final MapFeature<?> mapFeature;
    private final ResolvedMapAttributes attributes;
    private final String featureId;
    private final String categoryId;
    private final MapIcon icon;

    private List<Component> generatedTooltip = new ArrayList<>();

    public FeatureEntryWidget(int x, int y, int width, int height, MapFeature<?> mapFeature) {
        super(x, y, width, height, Component.literal(mapFeature.getFeatureId()));
        this.mapFeature = mapFeature;

        this.attributes = Services.MapData.resolveMapAttributes(mapFeature);
        this.featureId = mapFeature.getFeatureId();
        this.categoryId = mapFeature.getCategoryId();

        this.icon = mapFeature instanceof MapLocation
                ? Services.MapData.getIcon(attributes.iconId()).orElse(null)
                : null;

        generateTooltip();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND, getX(), getY(), this.width, this.height);

        int textX;

        if (icon != null) {
            int iconBoxSize = this.height - PADDING * 2;
            int iconBoxX = getX() + PADDING;
            int iconBoxY = getY() + PADDING;

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, iconBoxX, iconBoxY, iconBoxSize, iconBoxSize);

            int iconWidth = icon.getWidth();
            int iconHeight = icon.getHeight();

            float scale = Math.min(1f, (float) (iconBoxSize - ICON_BORDER * 2) / Math.max(iconWidth, iconHeight));
            int drawWidth = Math.max(1, Math.round(iconWidth * scale));
            int drawHeight = Math.max(1, Math.round(iconHeight * scale));

            RenderUtils.drawScalingTexturedRect(
                    guiGraphics,
                    icon.getIdentifier(),
                    attributes.iconColor(),
                    iconBoxX + (iconBoxSize - drawWidth) / 2f,
                    iconBoxY + (iconBoxSize - drawHeight) / 2f,
                    drawWidth,
                    drawHeight,
                    iconWidth,
                    iconHeight);

            textX = iconBoxX + iconBoxSize + TEXT_GAP;
        } else {
            textX = getX() + TEXT_GAP;
        }

        int maxTextWidth = getX() + this.width - textX - PADDING;

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(featureId),
                        textX,
                        getY() + 9,
                        maxTextWidth,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal(categoryId).withStyle(ChatFormatting.GRAY)),
                        textX,
                        getY() + this.height - 9,
                        maxTextWidth,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && mapFeature instanceof MapLocation mapLocation) {
            Services.UserMarker.removeAllUserMarkedFeatures();
            Services.UserMarker.addUserMarkedFeature(mapLocation);

            McUtils.playSoundUI(SoundEvents.EXPERIENCE_ORB_PICKUP);
        } else if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            McUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());

            McUtils.setScreen(CategoryManagementScreen.create(McUtils.mc().screen, categoryId, getOverrideType()));
            return true;
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }

    private OverrideType getOverrideType() {
        if (mapFeature instanceof MapPath) {
            return OverrideType.MAP_PATH_OVERRIDE;
        } else if (mapFeature instanceof MapArea) {
            return OverrideType.MAP_AREA_OVERRIDE;
        }

        return OverrideType.MAP_LOCATION_OVERRIDE;
    }

    private void generateTooltip() {
        this.generatedTooltip = new ArrayList<>();

        this.generatedTooltip.add(Component.literal(featureId).withStyle(ChatFormatting.GOLD));

        if (mapFeature instanceof MapLocation) {
            addLine(
                    Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.type"),
                    Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.mapLocation")
                            .withStyle(ChatFormatting.WHITE));
        } else if (mapFeature instanceof MapPath) {
            addLine(
                    Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.type"),
                    Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.mapPath")
                            .withStyle(ChatFormatting.WHITE));
        } else if (mapFeature instanceof MapArea) {
            addLine(
                    Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.type"),
                    Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.mapArea")
                            .withStyle(ChatFormatting.WHITE));
        }

        addLine(
                Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.label"),
                attributes.label());
        addLine(
                Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.categoryId"),
                categoryId);

        if (mapFeature instanceof MapLocation) {
            String iconId = attributes.iconId();

            if (!iconId.equals(MapIcon.NO_ICON_ID)) {
                MutableComponent iconValue = Component.literal(iconId).withStyle(ChatFormatting.WHITE);

                if (icon == null) {
                    iconValue.append(Component.translatable(
                                    "screens.wynntils.map.managers.providerManager.entryWidgets.notFound")
                            .withStyle(ChatFormatting.RED));
                }

                addLine(
                        Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.iconId"),
                        iconValue);
            }
        }

        if (mapFeature instanceof MapLocation mapLocation) {
            Location location = mapLocation.getLocation();
            addLine(
                    Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.location"),
                    "%d, %d, %d".formatted(location.x(), location.y(), location.z()));
        }

        this.generatedTooltip.add(Component.empty());

        if (mapFeature instanceof MapLocation) {
            this.generatedTooltip.add(Component.empty()
                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.translatable(
                                    "screens.wynntils.map.managers.providerManager.entryWidgets.leftClickToSetMarker")
                            .withStyle(ChatFormatting.GREEN)));
        }

        this.generatedTooltip.add(Component.empty()
                .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.translatable(
                                "screens.wynntils.map.managers.providerManager.entryWidgets.rightClickToEditCategory")
                        .withStyle(ChatFormatting.GREEN)));
    }

    private void addLine(Component label, String value) {
        addLine(label, Component.literal(value).withStyle(ChatFormatting.WHITE));
    }

    private void addLine(Component label, Component value) {
        this.generatedTooltip.add(Component.literal("- ")
                .withStyle(ChatFormatting.GOLD)
                .append(label.copy().withStyle(ChatFormatting.GRAY))
                .append(value));
    }
}
