/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.GuildMapFeature;
import com.wynntils.models.territories.TerritoryInfo;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.screens.maps.widgets.MapButton;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.services.map.pois.TerritoryPoi;
import com.wynntils.services.map.pois.WaypointPoi;
import com.wynntils.services.map.type.TerritoryFilterType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.BoundingBox;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class GuildMapScreen extends AbstractMapScreen {
    private boolean resourceMode = false;
    private boolean territoryNameMode = false;
    private boolean territoryDefenseFilterEnabled = false;
    private boolean territoryTreasuryFilterEnabled = false;
    private boolean hybridMode = true;

    private GuildResourceValues territoryDefenseFilterLevel = GuildResourceValues.VERY_HIGH;
    private GuildResourceValues territoryTreasuryFilterLevel = GuildResourceValues.VERY_HIGH;
    private TerritoryFilterType territoryDefenseFilterType = TerritoryFilterType.DEFAULT;
    private TerritoryFilterType territoryTreasuryFilterType = TerritoryFilterType.DEFAULT;

    private MapButton territoryDefenseFilterButton;
    private MapButton territoryTreasuryFilterButton;
    private MapButton hybridModeButton;

    private GuildMapScreen() {}

    private GuildMapScreen(float mapCenterX, float mapCenterZ, float zoomLevel) {
        super(mapCenterX, mapCenterZ, zoomLevel);
    }

    public static Screen create() {
        return new GuildMapScreen();
    }

    public static Screen create(float mapCenterX, float mapCenterZ, float zoomLevel) {
        return new GuildMapScreen(mapCenterX, mapCenterZ, zoomLevel);
    }

    @Override
    protected void doInit() {
        super.doInit();

        addMapButton(new MapButton(
                Texture.ADD_ICON,
                (b) -> resourceMode = !resourceMode,
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.translatable("screens.wynntils.guildMap.toggleResourceColor.name")),
                        Component.translatable("screens.wynntils.guildMap.toggleResourceColor.description")
                                .withStyle(ChatFormatting.GRAY))));

        addMapButton(new MapButton(
                Texture.SIGN_ICON,
                (b) -> territoryNameMode = !territoryNameMode,
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.translatable("screens.wynntils.guildMap.toggleTerritoryNames.name")),
                        Component.translatable("screens.wynntils.guildMap.toggleTerritoryNames.description")
                                .withStyle(ChatFormatting.GRAY))));

        territoryDefenseFilterButton = new MapButton(
                Texture.DEFENSE_FILTER_ICON,
                (b) -> {
                    // Left and right clicks cycle through the defense levels, middle click resets to OFF
                    if (b == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                        territoryDefenseFilterEnabled = false;
                        territoryDefenseFilterType = TerritoryFilterType.DEFAULT;
                        territoryDefenseFilterButton.setTooltip(getCompleteDefenseFilterTooltip());
                        return;
                    }

                    // Holding shift filters higher, ctrl filters lower
                    if (KeyboardUtils.isShiftDown()) {
                        territoryDefenseFilterType = TerritoryFilterType.HIGHER;
                    } else if (KeyboardUtils.isControlDown()) {
                        territoryDefenseFilterType = TerritoryFilterType.LOWER;
                    } else {
                        territoryDefenseFilterType = TerritoryFilterType.DEFAULT;
                    }

                    territoryDefenseFilterEnabled = true;
                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        territoryDefenseFilterLevel = territoryDefenseFilterLevel.getFilterNext(
                                territoryDefenseFilterType != TerritoryFilterType.DEFAULT);
                    } else if (b == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        territoryDefenseFilterLevel = territoryDefenseFilterLevel.getFilterPrevious(
                                territoryDefenseFilterType != TerritoryFilterType.DEFAULT);
                    }

                    territoryDefenseFilterButton.setTooltip(getCompleteDefenseFilterTooltip());
                },
                getCompleteDefenseFilterTooltip());
        addMapButton(territoryDefenseFilterButton);

        territoryTreasuryFilterButton = new MapButton(
                Texture.TREASURY,
                (b) -> {
                    // Left and right clicks cycle through the treasury levels, middle click resets to OFF
                    if (b == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                        territoryTreasuryFilterEnabled = false;
                        territoryTreasuryFilterType = TerritoryFilterType.DEFAULT;
                        territoryTreasuryFilterButton.setTooltip(getCompleteTreasuryFilterTooltip());
                        return;
                    }

                    // Holding shift filters higher, ctrl filters lower
                    if (KeyboardUtils.isShiftDown()) {
                        territoryTreasuryFilterType = TerritoryFilterType.HIGHER;
                    } else if (KeyboardUtils.isControlDown()) {
                        territoryTreasuryFilterType = TerritoryFilterType.LOWER;
                    } else {
                        territoryTreasuryFilterType = TerritoryFilterType.DEFAULT;
                    }

                    territoryTreasuryFilterEnabled = true;
                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        territoryTreasuryFilterLevel = territoryTreasuryFilterLevel.getFilterNext(
                                territoryTreasuryFilterType != TerritoryFilterType.DEFAULT);
                    } else if (b == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        territoryTreasuryFilterLevel = territoryTreasuryFilterLevel.getFilterPrevious(
                                territoryTreasuryFilterType != TerritoryFilterType.DEFAULT);
                    }

                    territoryTreasuryFilterButton.setTooltip(getCompleteTreasuryFilterTooltip());
                },
                getCompleteTreasuryFilterTooltip());
        addMapButton(territoryTreasuryFilterButton);

        hybridModeButton = new MapButton(
                Texture.OVERLAY_EXTRA_ICON,
                (b) -> {
                    hybridMode = !hybridMode;
                    hybridModeButton.setTooltip(getHybridModeTooltip());
                },
                getHybridModeTooltip());
        addMapButton(hybridModeButton);

        addMapButton(new MapButton(
                Texture.MAP,
                (b) -> changeToMainMap(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.BLUE)
                                .append(Component.translatable("screens.wynntils.guildMap.mainMap.name")),
                        Component.translatable("screens.wynntils.guildMap.mainMap.description")
                                .withStyle(ChatFormatting.GRAY))));

        addMapButton(new MapButton(
                Texture.HELP_ICON,
                (b) -> {},
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.translatable("screens.wynntils.map.help.name")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.guildMap.help.description1")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.guildMap.help.description2")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.guildMap.help.description3")))));

        if (firstInit) {
            // When outside of the main map, center to the middle of the map
            if (!isPlayerInsideMainArea()) {
                centerMapOnWorld();
            }

            firstInit = false;
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (holdingMapKey
                && !Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .openGuildMapKeybind
                        .getKeyMapping()
                        .isDown()) {
            this.onClose();
            return;
        }

        renderMap(guiGraphics);

        RenderUtils.enableScissor(
                guiGraphics,
                (int) (renderX + renderedBorderXOffset),
                (int) (renderY + renderedBorderYOffset),
                (int) mapWidth,
                (int) mapHeight);

        renderPois(guiGraphics, mouseX, mouseY);

        renderCursor(
                guiGraphics,
                1.5f,
                Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .pointerColor
                        .get(),
                Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .pointerType
                        .get());

        RenderUtils.disableScissor(guiGraphics);

        renderMapBorder(guiGraphics);

        renderCoordinates(guiGraphics, mouseX, mouseY);

        renderMapButtons(guiGraphics, mouseX, mouseY, partialTick);

        renderZoomWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderHoveredTerritoryInfo(guiGraphics);

        if (isPanning) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_ALL);
        } else if (holdingZoomHandle) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if ((this.hovered != null && !(this.hovered instanceof TerritoryPoi))
                || isMouseOverZoomHandle(mouseX, mouseY)) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    public void changeToMainMap() {
        McUtils.mc().setScreen(MainMapScreen.create(mapCenterX, mapCenterZ, zoomLevel));
    }

    @Override
    protected void renderPois(
            List<Poi> pois,
            GuiGraphics guiGraphics,
            BoundingBox textureBoundingBox,
            float poiScale,
            int mouseX,
            int mouseY) {
        hovered = null;

        List<Poi> filteredPois = getRenderedPois(pois, textureBoundingBox, poiScale, mouseX, mouseY);

        // Render trading routes
        // We render them in both directions because optimizing it is not cheap either
        for (Poi poi : filteredPois) {
            if (!(poi instanceof TerritoryPoi territoryPoi)) continue;

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, zoomRenderScale);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, zoomRenderScale);

            for (String tradingRoute : territoryPoi.getTerritoryInfo().getTradingRoutes()) {
                Optional<Poi> routePoi = filteredPois.stream()
                        .filter(filteredPoi -> filteredPoi.getName().equals(tradingRoute))
                        .findFirst();

                // Only render connection if the other poi is also in the filtered pois
                if (routePoi.isPresent() && filteredPois.contains(routePoi.get())) {
                    float x = MapRenderer.getRenderX(routePoi.get(), mapCenterX, centerX, zoomRenderScale);
                    float z = MapRenderer.getRenderZ(routePoi.get(), mapCenterZ, centerZ, zoomRenderScale);

                    RenderUtils.drawLine(guiGraphics, CommonColors.DARK_GRAY, poiRenderX, poiRenderZ, x, z, 1);
                }
            }
        }

        // Reverse and Render
        for (int i = filteredPois.size() - 1; i >= 0; i--) {
            Poi poi = filteredPois.get(i);

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, zoomRenderScale);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, zoomRenderScale);

            poi.renderAt(
                    guiGraphics, poiRenderX, poiRenderZ, hovered == poi, poiScale, zoomRenderScale, zoomLevel, true);
        }
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        for (GuiEventListener child :
                Stream.concat(children().stream(), mapButtons.stream()).toList()) {
            if (child.isMouseOver(event.x(), event.y())) {
                child.mouseClicked(event, isDoubleClick);
                return true;
            }
        }

        // Manage on shift right click
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                && KeyboardUtils.isShiftDown()
                && hovered instanceof TerritoryPoi territoryPoi) {
            Handlers.Command.queueCommand("gu territory " + territoryPoi.getName());
        } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (hovered instanceof WaypointPoi) {
                Models.Marker.USER_WAYPOINTS_PROVIDER.removeLocation(
                        hovered.getLocation().asLocation());
                return true;
            }
        } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            setCompassToMouseCoords(event.x(), event.y(), !KeyboardUtils.isShiftDown());
            return true;
        }

        return super.doMouseClicked(event, isDoubleClick);
    }

    private void renderHoveredTerritoryInfo(GuiGraphics guiGraphics) {
        if (!(hovered instanceof TerritoryPoi territoryPoi)) return;

        int xOffset = (int) (width - SCREEN_SIDE_OFFSET - 250);
        int yOffset = (int) (SCREEN_SIDE_OFFSET + 40);

        if (territoryPoi.isFakeTerritoryInfo()) {
            renderTerritoryTooltipWithFakeInfo(guiGraphics, xOffset, yOffset, territoryPoi);
        } else {
            renderTerritoryTooltip(guiGraphics, xOffset, yOffset, territoryPoi);
        }
    }

    private void renderPois(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<TerritoryPoi> advancementPois = Models.Territory.getTerritoryPoisFromAdvancement().stream()
                .filter(this::filterDefense)
                .filter(this::filterTreasury)
                .toList();

        List<Poi> renderedPois = new ArrayList<>();

        if (hybridMode) {
            // We base hybrid mode on the advancement pois, it should be more consistent

            for (TerritoryPoi poi : advancementPois) {
                TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfile(poi.getName());

                // If the API and advancement pois don't match, we use the API pois without advancement info
                if (territoryProfile != null
                        && territoryProfile
                                .getGuild()
                                .equals(poi.getTerritoryInfo().getGuildName())) {
                    renderedPois.add(poi);
                } else {
                    renderedPois.add(new TerritoryPoi(territoryProfile, poi.getTerritoryInfo()));
                }
            }
        } else {
            renderedPois.addAll(advancementPois);
        }

        Models.Marker.USER_WAYPOINTS_PROVIDER.getPois().forEach(renderedPois::add);

        renderPois(
                renderedPois,
                guiGraphics,
                BoundingBox.centered(mapCenterX, mapCenterZ, width / zoomRenderScale, height / zoomRenderScale),
                1,
                mouseX,
                mouseY);
    }

    public boolean isResourceMode() {
        return resourceMode;
    }

    public boolean isTerritoryNameMode() {
        return territoryNameMode;
    }

    private boolean filterDefense(TerritoryPoi territoryArea) {
        return !territoryDefenseFilterEnabled
                || filterTerritory(
                        territoryArea,
                        territoryDefenseFilterType,
                        territoryDefenseFilterLevel,
                        area -> area.getTerritoryInfo().getDefences());
    }

    private boolean filterTreasury(TerritoryPoi territoryArea) {
        return !territoryTreasuryFilterEnabled
                || filterTerritory(
                        territoryArea,
                        territoryTreasuryFilterType,
                        territoryTreasuryFilterLevel,
                        area -> area.getTerritoryInfo().getTreasury());
    }

    private boolean filterTerritory(
            TerritoryPoi territoryArea,
            TerritoryFilterType filterType,
            GuildResourceValues filterLevel,
            Function<TerritoryPoi, GuildResourceValues> getter) {
        GuildResourceValues guildResourceValue = getter.apply(territoryArea);
        if (guildResourceValue == null) return false;

        return switch (filterType) {
            case HIGHER -> guildResourceValue.getLevel() >= filterLevel.getLevel();
            case LOWER -> guildResourceValue.getLevel() <= filterLevel.getLevel();
            case DEFAULT -> guildResourceValue.getLevel() == filterLevel.getLevel();
        };
    }

    private static void renderTerritoryTooltip(
            GuiGraphics guiGraphics, int xOffset, int yOffset, TerritoryPoi territoryPoi) {
        final TerritoryInfo territoryInfo = territoryPoi.getTerritoryInfo();
        final TerritoryProfile territoryProfile = territoryPoi.getTerritoryProfile();

        final int textureWidth = Texture.MAP_INFO_TOOLTIP_CENTER.width();

        final float centerHeight = 75
                + (territoryInfo.getStorage().size()
                                + territoryInfo.getGenerators().size())
                        * 10
                + (territoryInfo.isHeadquarters() ? 20 : 0);

        RenderUtils.drawTexturedRect(guiGraphics, Texture.MAP_INFO_TOOLTIP_TOP, xOffset, yOffset);
        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.MAP_INFO_TOOLTIP_CENTER.identifier(),
                xOffset,
                Texture.MAP_INFO_TOOLTIP_TOP.height() + yOffset,
                textureWidth,
                centerHeight,
                textureWidth,
                Texture.MAP_INFO_TOOLTIP_CENTER.height());
        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.MAP_INFO_NAME_BOX,
                xOffset,
                Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight + yOffset);

        // guild
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(
                                "%s [%s]".formatted(territoryInfo.getGuildName(), territoryInfo.getGuildPrefix())),
                        10 + xOffset,
                        10 + yOffset,
                        CommonColors.MAGENTA,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        float renderYOffset = 20 + yOffset;

        for (GuildResource value : GuildResource.values()) {
            int generation = territoryInfo.getGeneration(value);
            CappedValue storage = territoryInfo.getStorage(value);

            if (generation != 0) {
                StyledText formattedGenerated = StyledText.fromString(
                        "%s+%d %s per Hour".formatted(value.getPrettySymbol(), generation, value.getName()));

                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                formattedGenerated,
                                10 + xOffset,
                                10 + renderYOffset,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.TOP,
                                TextShadow.OUTLINE);
                renderYOffset += 10;
            }

            if (storage != null) {
                StyledText formattedStored = StyledText.fromString("%s%d/%d %s stored"
                        .formatted(value.getPrettySymbol(), storage.current(), storage.max(), value.getName()));

                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                formattedStored,
                                10 + xOffset,
                                10 + renderYOffset,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.TOP,
                                TextShadow.OUTLINE);
                renderYOffset += 10;
            }
        }

        renderYOffset += 10;

        StyledText treasury = StyledText.fromString(ChatFormatting.GRAY
                + "✦ Treasury: %s"
                        .formatted(territoryInfo.getTreasury().getTreasuryColor()
                                + territoryInfo.getTreasury().getAsString()));
        StyledText defences = StyledText.fromString(ChatFormatting.GRAY
                + "Territory Defences: %s"
                        .formatted(territoryInfo.getDefences().getDefenceColor()
                                + territoryInfo.getDefences().getAsString()));

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        treasury,
                        10 + xOffset,
                        10 + renderYOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);
        renderYOffset += 10;
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        defences,
                        10 + xOffset,
                        10 + renderYOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        if (territoryInfo.isHeadquarters()) {
            renderYOffset += 20;
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Guild Headquarters"),
                            10 + xOffset,
                            10 + renderYOffset,
                            CommonColors.RED,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        }

        renderYOffset += 20;

        String timeHeldString = territoryProfile.getGuild().equals(territoryInfo.getGuildName())
                ? territoryProfile.getTimeAcquiredColor() + territoryProfile.getReadableRelativeTimeAcquired()
                : "-";
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(ChatFormatting.GRAY + "Time Held: " + timeHeldString),
                        10 + xOffset,
                        10 + renderYOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        // Territory name
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(territoryPoi.getName()),
                        7 + xOffset,
                        textureWidth + xOffset,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight + yOffset,
                        Texture.MAP_INFO_TOOLTIP_TOP.height()
                                + centerHeight
                                + Texture.MAP_INFO_NAME_BOX.height()
                                + yOffset,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }

    private static void renderTerritoryTooltipWithFakeInfo(
            GuiGraphics guiGraphics, int xOffset, int yOffset, TerritoryPoi territoryPoi) {
        final TerritoryInfo territoryInfo = territoryPoi.getTerritoryInfo();
        final TerritoryProfile territoryProfile = territoryPoi.getTerritoryProfile();

        final int textureWidth = Texture.MAP_INFO_TOOLTIP_CENTER.width();

        final float centerHeight = 35;

        RenderUtils.drawTexturedRect(guiGraphics, Texture.MAP_INFO_TOOLTIP_TOP, xOffset, yOffset);
        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.MAP_INFO_TOOLTIP_CENTER.identifier(),
                (float) xOffset,
                (float) (Texture.MAP_INFO_TOOLTIP_TOP.height() + yOffset),
                textureWidth,
                (int) centerHeight,
                textureWidth,
                Texture.MAP_INFO_TOOLTIP_CENTER.height());
        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.MAP_INFO_NAME_BOX,
                xOffset,
                Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight + yOffset);

        // guild
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(
                                "%s [%s]".formatted(territoryProfile.getGuild(), territoryProfile.getGuildPrefix())),
                        10 + xOffset,
                        10 + yOffset,
                        CommonColors.MAGENTA,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.guildMap.hybridMode.noAdvancementData")),
                        10 + xOffset,
                        30 + yOffset,
                        CommonColors.LIGHT_GRAY,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        // Territory name
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(territoryPoi.getName()),
                        7 + xOffset,
                        textureWidth + xOffset,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight + yOffset,
                        Texture.MAP_INFO_TOOLTIP_TOP.height()
                                + centerHeight
                                + Texture.MAP_INFO_NAME_BOX.height()
                                + yOffset,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }

    private List<Component> getCompleteDefenseFilterTooltip() {
        Component lastLine = territoryDefenseFilterEnabled
                ? Component.translatable("screens.wynntils.guildMap.cycleFilter.description3")
                        .withStyle(ChatFormatting.GRAY)
                        .append(territoryDefenseFilterLevel.getDefenceColor()
                                + territoryDefenseFilterLevel.getAsString())
                        .append(territoryDefenseFilterType.asComponent())
                : Component.translatable("screens.wynntils.guildMap.cycleFilter.description3")
                        .withStyle(ChatFormatting.GRAY)
                        .append("Off");
        return List.of(
                Component.literal("[>] ")
                        .withStyle(ChatFormatting.BLUE)
                        .append(Component.translatable("screens.wynntils.guildMap.cycleDefenseFilter.name")),
                Component.translatable("screens.wynntils.guildMap.cycleDefenseFilter.description")
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("screens.wynntils.guildMap.cycleFilter.description1")
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("screens.wynntils.guildMap.cycleFilter.description2")
                        .withStyle(ChatFormatting.GRAY),
                lastLine);
    }

    private List<Component> getCompleteTreasuryFilterTooltip() {
        Component lastLine = territoryTreasuryFilterEnabled
                ? Component.translatable("screens.wynntils.guildMap.cycleFilter.description3")
                        .withStyle(ChatFormatting.GRAY)
                        .append(territoryTreasuryFilterLevel.getTreasuryColor()
                                + territoryTreasuryFilterLevel.getAsString())
                        .append(territoryTreasuryFilterType.asComponent())
                : Component.translatable("screens.wynntils.guildMap.cycleFilter.description3")
                        .withStyle(ChatFormatting.GRAY)
                        .append("Off");
        return List.of(
                Component.literal("[>] ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.translatable("screens.wynntils.guildMap.cycleTerritoryFilter.name")),
                Component.translatable("screens.wynntils.guildMap.cycleTerritoryFilter.description")
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("screens.wynntils.guildMap.cycleFilter.description1")
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("screens.wynntils.guildMap.cycleFilter.description2")
                        .withStyle(ChatFormatting.GRAY),
                lastLine);
    }

    private List<Component> getHybridModeTooltip() {
        return List.of(
                Component.literal("[>] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.translatable("screens.wynntils.guildMap.hybridMode.name")),
                Component.translatable("screens.wynntils.guildMap.hybridMode.description1")
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("screens.wynntils.guildMap.hybridMode.description2")
                        .withStyle(ChatFormatting.GRAY)
                        .append(
                                (hybridMode
                                        ? Component.translatable("screens.wynntils.guildMap.hybridMode.hybrid")
                                                .withStyle(ChatFormatting.GREEN)
                                        : Component.translatable("screens.wynntils.guildMap.hybridMode.advancement")
                                                .withStyle(ChatFormatting.RED))));
    }
}
