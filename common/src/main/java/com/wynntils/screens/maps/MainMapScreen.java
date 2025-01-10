/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.features.debug.MappingProgressFeature;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.screens.maps.widgets.MapButton;
import com.wynntils.services.lootrunpaths.LootrunPathInstance;
import com.wynntils.services.map.pois.CustomPoi;
import com.wynntils.services.mapdata.features.builtin.TerritoryArea;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class MainMapScreen extends AbstractMapScreen {
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
                (b) -> McUtils.mc().setScreen(PoiCreationScreen.create(this)),
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

                    //                    List<MarkerInfo> markers = Models.Marker.USER_WAYPOINTS_PROVIDER
                    //                            .getMarkerInfos()
                    //                            .toList();
                    //                    if (!markers.isEmpty()) {
                    //                        // -1 is fine as the index since we always increment it by 1
                    //                        int index = markers.indexOf(focusedMarker);
                    //                        MarkerInfo markerInfo = markers.get((index + 1) % markers.size());
                    //                        focusedMarker = markerInfo;
                    //                        Location location = markerInfo.location();
                    //                        updateMapCenter(location.x, location.z);
                    //                    }
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
                (b) -> McUtils.mc().setScreen(PoiManagementScreen.create(this)),
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

        // FIXME: Add back the pois that are still not converted to MapData
        //        - Provided custom pois
        //        - Marker waypoints
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
            //            List<MarkerInfo> markers =
            //                    Models.Marker.USER_WAYPOINTS_PROVIDER.getMarkerInfos().toList();
            //            if (KeyboardUtils.isShiftDown() && !markers.isEmpty()) {
            //                // -1 is fine as the index since we always increment it by 1
            //                int index = markers.indexOf(focusedMarker);
            //                MarkerInfo markerInfo = markers.get((index + 1) % markers.size());
            //                focusedMarker = markerInfo;
            //                Location location = markerInfo.location();
            //                updateMapCenter(location.x, location.z);
            //                return true;
            //            }

            centerMapAroundPlayer();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            //            if (hovered instanceof WaypointPoi) {
            //                Models.Marker.USER_WAYPOINTS_PROVIDER.removeLocation(
            //                        hovered.getLocation().asLocation());
            //                return true;
            //            }

            if (hoveredFeature != null && !(hoveredFeature instanceof TerritoryArea)) {
                //                McUtils.playSoundUI(SoundEvents.EXPERIENCE_ORB_PICKUP);
                //
                //                // If shift is not held down, clear all waypoints to only have the new one
                //                if (!KeyboardUtils.isShiftDown()) {
                //                    Models.Marker.USER_WAYPOINTS_PROVIDER.removeAllLocations();
                //                }
                //
                //                if (hovered.hasStaticLocation()) {
                //                    if (hovered instanceof IconPoi iconPoi) {
                //                        if (iconPoi instanceof CustomPoi customPoi) {
                //                            Models.Marker.USER_WAYPOINTS_PROVIDER.addLocation(
                //                                    new Location(hovered.getLocation()),
                //                                    iconPoi.getIcon(),
                //                                    customPoi.getColor(),
                //                                    customPoi.getColor(),
                //                                    hovered.getName());
                //                        } else {
                //                            Models.Marker.USER_WAYPOINTS_PROVIDER.addLocation(
                //                                    new Location(hovered.getLocation()), iconPoi.getIcon(),
                // hovered.getName());
                //                        }
                //                    } else {
                //                        Models.Marker.USER_WAYPOINTS_PROVIDER.addLocation(
                //                                new Location(hovered.getLocation()), hovered.getName());
                //                    }
                //                } else {
                //                    final Poi finalHovered = hovered;
                //                    Models.Marker.USER_WAYPOINTS_PROVIDER.addLocation(
                //                            new DynamicLocationSupplier(
                //                                    () -> finalHovered.getLocation().asLocation()),
                //                            finalHovered.getName());
                //                }
                //                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            if (KeyboardUtils.isShiftDown()) {
                if (hoveredFeature instanceof CustomPoi customPoi && !Services.Poi.isPoiProvided(customPoi)) {
                    McUtils.mc().setScreen(PoiCreationScreen.create(this, customPoi));
                } else {
                    int gameX = (int) ((mouseX - centerX) / zoomRenderScale + mapCenterX);
                    int gameZ = (int) ((mouseY - centerZ) / zoomRenderScale + mapCenterZ);

                    McUtils.mc().setScreen(PoiCreationScreen.create(this, new Location(gameX, 0, gameZ)));
                }
            } else if (KeyboardUtils.isAltDown()) {
                if (hoveredFeature instanceof CustomPoi customPoi && !Services.Poi.isPoiProvided(customPoi)) {
                    HiddenConfig<List<CustomPoi>> customPois =
                            Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;
                    customPois.get().remove(customPoi);
                    customPois.touched();
                }
            } else {
                setCompassToMouseCoords(mouseX, mouseY, true);
                return true;
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    private void shareLocationOrCompass(int button) {
        //        List<MarkerInfo> markers =
        //                Models.Marker.USER_WAYPOINTS_PROVIDER.getMarkerInfos().toList();
        //
        //        boolean shareCompass = KeyboardUtils.isShiftDown() && !markers.isEmpty();
        //
        //        String target = null;
        //
        //        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
        //            target = "guild";
        //        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
        //            target = "party";
        //        }
        //
        //        if (target == null) return;
        //
        //        if (shareCompass) {
        //            // FIXME: Find an intuitive way to share compasses with multiple waypoints
        //            LocationUtils.shareCompass(target, markers.getFirst().location());
        //        } else {
        //            LocationUtils.shareLocation(target);
        //        }
    }
}
