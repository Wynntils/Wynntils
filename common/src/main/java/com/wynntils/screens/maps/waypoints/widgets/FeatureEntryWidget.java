/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.waypoints.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.waypoints.WaypointCategoryScreen;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.builtin.WaypointLocation;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class FeatureEntryWidget extends AbstractWidget implements TooltipProvider {
    private static final int PADDING = 4;
    private static final int ICON_BORDER = 2;
    private static final int TEXT_GAP = 6;

    private final ResolvedMapAttributes attributes;
    private final String featureId;
    private final String categoryId;
    private final Location location;
    private final MapIcon icon;
    private final WaypointCategoryScreen parent;

    private List<Component> generatedTooltip = new ArrayList<>();

    public FeatureEntryWidget(
            int x, int y, int width, int height, WaypointLocation waypoint, WaypointCategoryScreen parent) {
        super(x, y, width, height, Component.literal(waypoint.getFeatureId()));

        this.attributes = Services.MapData.resolveMapAttributes(waypoint);
        this.featureId = waypoint.getFeatureId();
        this.categoryId = waypoint.getCategoryId();
        this.location = waypoint.getLocation();
        this.icon = Services.MapData.getIcon(attributes.iconId()).orElse(null);
        this.parent = parent;

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

        Component coordsLine = Component.literal(location.asChatCoordinates())
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(" (%dm)"
                                .formatted(Math.round(Math.sqrt(parent.creationScreen
                                        .getWaypoint()
                                        .getLocation()
                                        .distanceToSqr(location.toPosition())))))
                        .withStyle(ChatFormatting.DARK_GRAY));

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromComponent(coordsLine),
                        textX,
                        getY() + this.height - 9,
                        maxTextWidth,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }

    private void generateTooltip() {
        this.generatedTooltip = new ArrayList<>();

        this.generatedTooltip.add(Component.literal(featureId).withStyle(ChatFormatting.GOLD));

        addLine(
                Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.label"),
                attributes.label());
        addLine(
                Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.categoryId"),
                categoryId);

        String iconId = attributes.iconId();

        if (!iconId.equals(MapIcon.NO_ICON_ID)) {
            MutableComponent iconValue = Component.literal(iconId).withStyle(ChatFormatting.WHITE);

            if (icon == null) {
                iconValue.append(
                        Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.notFound")
                                .withStyle(ChatFormatting.RED));
            }

            addLine(
                    Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.iconId"),
                    iconValue);
        }
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
