/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.features.debug.MappingProgressFeature;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.models.seaskipper.type.SeaskipperDestinationArea;
import com.wynntils.screens.maps.widgets.MapButton;
import com.wynntils.services.lootrunpaths.LootrunPathInstance;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.builtin.TerritoryArea;
import com.wynntils.services.mapdata.features.builtin.WaypointLocation;
import com.wynntils.services.mapdata.features.type.MapArea;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.features.type.MapLocation;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.wynn.LocationUtils;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public final class MainMapScreen extends AbstractMapScreen {
    private MapLocation focusedMarker = null;

    private MainMapScreen() {
        super();
        centerMapAroundPlayer();
    }

    private MainMapScreen(float mapCenterX, float mapCenterZ) {
        super(mapCenterX, mapCenterZ);
    }

    private MainMapScreen(float mapCenterX, float mapCenterZ, float zoomLevel) {
        super(mapCenterX, mapCenterZ, zoomLevel);
    }

    public static Screen create() {
        return new MainMapScreen();
    }

    public static Screen create(float mapCenterX, float mapCenterZ) {
        return new MainMapScreen(mapCenterX, mapCenterZ);
    }

    public static Screen create(float mapCenterX, float mapCenterZ, float zoomLevel) {
        return new MainMapScreen(mapCenterX, mapCenterZ, zoomLevel);
    }

    private boolean showTerrs = false;

    @Override
    protected void doInit() {
        super.doInit();

        addMapButton(new MapButton(
                Texture.ADD_ICON,
                (b) -> McUtils.mc().setScreen(WaypointCreationScreen.create(this)),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.DARK_GREEN)
                                .append(Component.translatable("screens.wynntils.map.waypoints.add.name")),
                        Component.translatable("screens.wynntils.map.waypoints.add.description")
                                .withStyle(ChatFormatting.GRAY))));

        addMapButton(new MapButton(
                Texture.WAYPOINT_FOCUS_ICON,
                (b) -> {
                    if (KeyboardUtils.isShiftDown()) {
                        centerMapAroundPlayer();
                        return;
                    }

                    focusNextMarkedLocation();
                },
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.translatable("screens.wynntils.map.focus.name")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.focus.description1")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.focus.description2")))));

        addMapButton(new MapButton(
                Texture.SHARE_ICON,
                this::shareLocationOrCompass,
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.DARK_AQUA)
                                .append(Component.translatable("screens.wynntils.map.share.name")),
                        Component.translatable("screens.wynntils.map.share.description1_1")
                                .withStyle(ChatFormatting.AQUA)
                                .append(Component.translatable("screens.wynntils.map.share.description1_2")
                                        .withStyle(ChatFormatting.GRAY)),
                        Component.translatable("screens.wynntils.map.share.description2_1")
                                .withStyle(ChatFormatting.AQUA)
                                .append(Component.translatable("screens.wynntils.map.share.description2_2")
                                        .withStyle(ChatFormatting.GRAY)),
                        Component.translatable("screens.wynntils.map.share.description3_1")
                                .withStyle(ChatFormatting.AQUA)
                                .append(Component.translatable("screens.wynntils.map.share.description3_2")
                                        .withStyle(ChatFormatting.GRAY)))));

        addMapButton(new MapButton(
                Texture.WAYPOINT_MANAGER_ICON,
                (b) -> McUtils.mc().setScreen(WaypointManagementScreen.create(this)),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.RED)
                                .append(Component.translatable("screens.wynntils.map.manager.name")),
                        Component.translatable("screens.wynntils.map.manager.description")
                                .withStyle(ChatFormatting.GRAY))));

        addMapButton(new MapButton(
                Texture.DEFENSE_FILTER_ICON,
                (b) -> McUtils.mc().setScreen(GuildMapScreen.create(mapCenterX, mapCenterZ, zoomLevel)),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.BLUE)
                                .append(Component.translatable("screens.wynntils.map.guildMap.name")),
                        Component.translatable("screens.wynntils.map.guildMap.description")
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
                                .append(Component.translatable("screens.wynntils.map.help.description1")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.help.description2")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.help.description3")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.help.description4")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.help.description5")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.help.description6")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.help.description7")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.help.description8")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.help.description9")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.help.description10")))));

        if (firstInit) {
            // When in an unmapped area, center to the middle of the map if the feature is enabled
            if (Managers.Feature.getFeatureInstance(MainMapFeature.class)
                            .centerWhenUnmapped
                            .get()
                    && Services.Map.getMapsForBoundingBox(mapBoundingBox).isEmpty()) {
                centerMapOnWorld();
            }

            firstInit = false;
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        if (holdingMapKey
                && !Managers.Feature.getFeatureInstance(MainMapFeature.class)
                        .openMapKeybind
                        .getKeyMapping()
                        .isDown()) {
            this.onClose();
            return;
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();

        renderMap(poseStack);

        RenderUtils.enableScissor(
                (int) (renderX + renderedBorderXOffset), (int) (renderY + renderedBorderYOffset), (int) mapWidth, (int)
                        mapHeight);

        renderMapFeatures(poseStack, mouseX, mouseY);

        if (Managers.Feature.getFeatureInstance(MappingProgressFeature.class).isEnabled()) {
            renderChunkBorders(poseStack);
            BUFFER_SOURCE.endBatch();
        }

        // Cursor
        renderCursor(
                poseStack,
                Managers.Feature.getFeatureInstance(MainMapFeature.class)
                        .playerPointerScale
                        .get(),
                Managers.Feature.getFeatureInstance(MainMapFeature.class)
                        .pointerColor
                        .get(),
                Managers.Feature.getFeatureInstance(MainMapFeature.class)
                        .pointerType
                        .get());

        LootrunPathInstance currentLootrun = Services.LootrunPaths.getCurrentLootrun();

        if (currentLootrun != null) {
            MapRenderer.renderLootrunLine(
                    currentLootrun,
                    2f,
                    3f,
                    poseStack,
                    centerX,
                    centerZ,
                    mapCenterX,
                    mapCenterZ,
                    zoomRenderScale,
                    CommonColors.LIGHT_BLUE.asInt(),
                    CommonColors.BLACK.asInt());
        }

        RenderUtils.disableScissor();

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        renderCoordinates(poseStack, mouseX, mouseY);

        renderZoomText(poseStack);

        renderMapButtons(guiGraphics, mouseX, mouseY, partialTick);

        renderZoomWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected Stream<MapFeature> getRenderedMapFeatures() {
        // Get all MapData features as Pois
        Stream<MapFeature> mapFeatures = Services.MapData.getFeatures();

        if (!KeyboardUtils.isControlDown()) {
            mapFeatures = mapFeatures.filter(feature -> !(feature instanceof TerritoryArea));
        }

        mapFeatures = mapFeatures.filter(feature -> !(feature instanceof SeaskipperDestinationArea));

        // FIXME: Add back the pois that are still not converted to MapData
        //        - Provided custom pois
        //        - Remote players

        return mapFeatures;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) {
            if (Managers.Feature.getFeatureInstance(MainMapFeature.class)
                    .holdGuildMapOpen
                    .get()) {
                showTerrs = true;
            } else {
                showTerrs = !showTerrs;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) {
            if (Managers.Feature.getFeatureInstance(MainMapFeature.class)
                    .holdGuildMapOpen
                    .get()) {
                showTerrs = false;
            }
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child :
                Stream.concat(children().stream(), mapButtons.stream()).toList()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (KeyboardUtils.isShiftDown()) {
                focusNextMarkedLocation();
                return true;
            }

            centerMapAroundPlayer();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (hoveredFeature instanceof MapLocation hoveredLocation) {
                McUtils.playSoundUI(SoundEvents.EXPERIENCE_ORB_PICKUP);

                if (Services.UserMarker.isUserMarkedFeature(hoveredLocation)) {
                    Services.UserMarker.removeUserMarkedFeature(hoveredLocation);
                    return true;
                }

                // If shift is not held down, clear all waypoints to only have the new one
                if (!KeyboardUtils.isShiftDown()) {
                    Services.UserMarker.removeAllUserMarkedFeatures();
                }

                Services.UserMarker.addUserMarkedFeature(hoveredLocation);

                return true;
            } else if (hoveredFeature instanceof MapArea mapArea) {
                McUtils.playSoundUI(SoundEvents.EXPERIENCE_ORB_PICKUP);

                Location centroid =
                        Location.containing(mapArea.getBoundingPolygon().centroid());
                if (Services.UserMarker.isMarkerAtLocation(centroid)) {
                    Services.UserMarker.removeMarkerAtLocation(centroid);
                    return true;
                }

                // If shift is not held down, clear all waypoints to only have the new one
                if (!KeyboardUtils.isShiftDown()) {
                    Services.UserMarker.removeAllUserMarkedFeatures();
                }

                ResolvedMapAttributes attributes = Services.MapData.resolveMapAttributes(mapArea);

                String label = attributes.label();

                // Special case for territories, use the territory name
                if (mapArea instanceof TerritoryArea territoryArea) {
                    label = territoryArea.getTerritoryProfile().getName();
                }

                Services.UserMarker.addMarkerAtLocation(centroid, label);

                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            if (KeyboardUtils.isShiftDown()) {
                if (hoveredFeature instanceof WaypointLocation location) {
                    McUtils.mc().setScreen(WaypointCreationScreen.create(this, location));
                } else {
                    int gameX = (int) ((mouseX - centerX) / zoomRenderScale + mapCenterX);
                    int gameZ = (int) ((mouseY - centerZ) / zoomRenderScale + mapCenterZ);

                    McUtils.mc().setScreen(WaypointCreationScreen.create(this, new Location(gameX, 0, gameZ)));
                }
            } else if (KeyboardUtils.isAltDown()) {
                if (hoveredFeature instanceof WaypointLocation waypointLocation) {
                    Services.Waypoints.removeWaypoint(waypointLocation);
                }
            } else {
                setCompassToMouseCoords(mouseX, mouseY, true);
                return true;
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    private void focusNextMarkedLocation() {
        List<MapLocation> markedLocations =
                Services.UserMarker.getMarkedFeatures().toList();
        if (markedLocations.isEmpty()) return;

        // Invalidate the focused marker if it's not marked anymore
        if (!Services.UserMarker.isUserMarkedFeature(focusedMarker)) {
            focusedMarker = null;
        }

        // -1 is fine as the index since we always increment it by 1
        int index = markedLocations.indexOf(focusedMarker);
        MapLocation mapLocation = markedLocations.get((index + 1) % markedLocations.size());
        focusedMarker = mapLocation;

        Location location = mapLocation.getLocation();
        updateMapCenter(location.x, location.z);
    }

    private void shareLocationOrCompass(int button) {
        List<MapLocation> markedLocations =
                Services.UserMarker.getMarkedFeatures().toList();

        boolean shareCompass = KeyboardUtils.isShiftDown() && !markedLocations.isEmpty();

        String target = null;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            target = "guild";
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            target = "party";
        }

        if (target == null) return;

        if (shareCompass) {
            LocationUtils.shareCompass(
                    target,
                    markedLocations.stream().map(MapLocation::getLocation).toList());
        } else {
            LocationUtils.shareLocation(target);
        }
    }
}
