/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.map.MapFeature;
import com.wynntils.models.map.PoiLocation;
import com.wynntils.models.map.pois.CustomPoi;
import com.wynntils.models.map.pois.IconPoi;
import com.wynntils.models.map.pois.PlayerMainMapPoi;
import com.wynntils.models.map.pois.Poi;
import com.wynntils.models.map.pois.TerritoryPoi;
import com.wynntils.models.map.pois.WaypointPoi;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.BoundingBox;
import com.wynntils.utils.wynn.LocationUtils;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public final class MainMapScreen extends AbstractMapScreen {
    private MainMapScreen() {
        super();
        centerMapAroundPlayer();
    }

    private MainMapScreen(float mapCenterX, float mapCenterZ) {
        super(mapCenterX, mapCenterZ);
        updateMapCenter(mapCenterX, mapCenterZ);
    }

    public static Screen create() {
        return new MainMapScreen();
    }

    public static Screen create(float mapCenterX, float mapCenterZ) {
        return new MainMapScreen(mapCenterX, mapCenterZ);
    }

    @Override
    protected void doInit() {
        super.doInit();

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20 * 6,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_HELP_BUTTON,
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
                                .append(Component.translatable("screens.wynntils.map.help.description9")))));

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20 * 3,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_MANAGER_BUTTON,
                (b) -> McUtils.mc().setScreen(PoiManagementScreen.create(this)),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.RED)
                                .append(Component.translatable("screens.wynntils.map.manager.name")),
                        Component.translatable("screens.wynntils.map.manager.description1")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.manager.description2")
                                        .withStyle(ChatFormatting.GRAY))
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.wynntils.map.manager.description3")
                                        .withStyle(ChatFormatting.GRAY)))));

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20 * 2,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_SHARE_BUTTON,
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

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_WAYPOINT_FOCUS_BUTTON,
                (b) -> {
                    if (KeyboardUtils.isShiftDown()) {
                        centerMapAroundPlayer();
                        return;
                    }

                    if (Models.Compass.getCompassLocation().isPresent()) {
                        Location location = Models.Compass.getCompassLocation().get();
                        updateMapCenter(location.x, location.z);
                    }
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

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_ADD_BUTTON,
                (b) -> McUtils.mc().setScreen(PoiCreationScreen.create(this)),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.DARK_GREEN)
                                .append(Component.translatable("screens.wynntils.map.waypoints.add.name")),
                        Component.translatable("screens.wynntils.map.waypoints.add.description")
                                .withStyle(ChatFormatting.GRAY))));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (holdingMapKey
                && !Managers.Feature.getFeatureInstance(MapFeature.class)
                        .openMapKeybind
                        .getKeyMapping()
                        .isDown()) {
            this.onClose();
            return;
        }

        updateMapCenterIfDragging(mouseX, mouseY);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();

        renderMap(
                poseStack,
                Managers.Feature.getFeatureInstance(MapFeature.class)
                        .renderUsingLinear
                        .get());

        RenderUtils.enableScissor(
                (int) (renderX + renderedBorderXOffset), (int) (renderY + renderedBorderYOffset), (int) mapWidth, (int)
                        mapHeight);

        renderPois(poseStack, mouseX, mouseY);

        // Cursor
        renderCursor(
                poseStack,
                Managers.Feature.getFeatureInstance(MapFeature.class)
                        .playerPointerScale
                        .get(),
                Managers.Feature.getFeatureInstance(MapFeature.class)
                        .pointerColor
                        .get(),
                Managers.Feature.getFeatureInstance(MapFeature.class)
                        .pointerType
                        .get());

        RenderUtils.disableScissor();

        renderBackground(poseStack);

        renderCoordinates(poseStack, mouseX, mouseY);

        renderMapButtons(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderPois(PoseStack poseStack, int mouseX, int mouseY) {
        Stream<? extends Poi> pois = Models.Map.getServicePois().stream();

        pois = Stream.concat(pois, Models.Map.getCombatPois().stream());
        pois = Stream.concat(pois, Models.Map.getLabelPois().stream());
        pois = Stream.concat(pois, Managers.Feature.getFeatureInstance(MapFeature.class).customPois.get().stream());
        pois = Stream.concat(pois, Models.Compass.getCompassWaypoint().stream());
        pois = Stream.concat(
                pois,
                Models.Hades.getHadesUsers()
                        .filter(
                                hadesUser -> (hadesUser.isPartyMember()
                                                && Managers.Feature.getFeatureInstance(MapFeature.class)
                                                        .renderRemotePartyPlayers
                                                        .get())
                                        || (hadesUser.isMutualFriend()
                                                && Managers.Feature.getFeatureInstance(MapFeature.class)
                                                        .renderRemoteFriendPlayers
                                                        .get())
                                /*|| (hadesUser.isGuildMember() && Managers.Feature.getFeatureInstance(MapFeature.class).renderRemoteGuildPlayers)*/ )
                        .map(PlayerMainMapPoi::new));

        if (KeyboardUtils.isControlDown()) {
            pois = Stream.concat(pois, Models.Territory.getTerritoryPois().stream());
        }

        renderPois(
                pois.collect(Collectors.toList()),
                poseStack,
                BoundingBox.centered(mapCenterX, mapCenterZ, width / currentZoom, height / currentZoom),
                Managers.Feature.getFeatureInstance(MapFeature.class).poiScale.get(),
                mouseX,
                mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (McUtils.mc().player.isShiftKeyDown()
                    && Models.Compass.getCompassLocation().isPresent()) {
                Location location = Models.Compass.getCompassLocation().get();
                updateMapCenter(location.x, location.z);
                return true;
            }

            centerMapAroundPlayer();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (hovered instanceof WaypointPoi) {
                Models.Compass.reset();
                return true;
            }

            if (hovered != null && !(hovered instanceof TerritoryPoi)) {
                McUtils.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
                if (hovered.hasStaticLocation()) {
                    if (hovered instanceof IconPoi iconPoi) {
                        if (iconPoi instanceof CustomPoi customPoi) {
                            Models.Compass.setCompassLocation(
                                    new Location(hovered.getLocation()), iconPoi.getIcon(), customPoi.getColor());
                        } else {
                            Models.Compass.setCompassLocation(new Location(hovered.getLocation()), iconPoi.getIcon());
                        }
                    } else {
                        Models.Compass.setCompassLocation(new Location(hovered.getLocation()));
                    }
                } else {
                    final Poi finalHovered = hovered;
                    Models.Compass.setDynamicCompassLocation(
                            () -> finalHovered.getLocation().asLocation());
                }
                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            if (KeyboardUtils.isShiftDown()) {
                if (hovered instanceof CustomPoi customPoi) {
                    McUtils.mc().setScreen(PoiCreationScreen.create(this, customPoi));
                } else {
                    int gameX = (int) ((mouseX - centerX) / currentZoom + mapCenterX);
                    int gameZ = (int) ((mouseY - centerZ) / currentZoom + mapCenterZ);

                    McUtils.mc().setScreen(PoiCreationScreen.create(this, new PoiLocation(gameX, null, gameZ)));
                }
            } else if (KeyboardUtils.isAltDown()) {
                if (hovered instanceof CustomPoi customPoi) {
                    Managers.Feature.getFeatureInstance(MapFeature.class)
                            .customPois
                            .get()
                            .remove(customPoi);
                    Managers.Config.saveConfig();
                }
            } else {
                setCompassToMouseCoords(mouseX, mouseY);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void setCompassToMouseCoords(double mouseX, double mouseY) {
        double gameX = (mouseX - centerX) / currentZoom + mapCenterX;
        double gameZ = (mouseY - centerZ) / currentZoom + mapCenterZ;
        Location compassLocation = Location.containing(gameX, 0, gameZ);
        Models.Compass.setCompassLocation(compassLocation);

        McUtils.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
    }

    private void shareLocationOrCompass(int button) {
        boolean shareCompass = KeyboardUtils.isShiftDown()
                && Models.Compass.getCompassLocation().isPresent();

        String target = null;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            target = "guild";
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            target = "party";
        }

        if (target == null) return;

        if (shareCompass) {
            LocationUtils.shareCompass(
                    target, Models.Compass.getCompassLocation().get());
        } else {
            LocationUtils.shareLocation(target);
        }
    }

    public void setHovered(Poi hovered) {
        this.hovered = hovered;
    }
}
