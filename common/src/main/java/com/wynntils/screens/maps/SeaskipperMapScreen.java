/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.features.map.GuildMapFeature;
import com.wynntils.models.items.items.gui.SeaskipperDestinationItem;
import com.wynntils.models.map.pois.Poi;
import com.wynntils.models.map.pois.SeaskipperPoi;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.BoundingBox;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class SeaskipperMapScreen extends AbstractMapScreen {
    private final AbstractContainerScreen<?> actualSeaskipperScreen;
    private final Map<SeaskipperDestinationItem, Integer> destinations = new HashMap<>();
    private final List<Poi> seaskipperPois = new ArrayList<>();

    private List<Poi> availableDestinations = new ArrayList<>();
    private Poi currentPoi;
    private boolean hideTerritoryBorders = false;
    private boolean renderAllDestinations = false;
    private boolean renderRoutes = false;
    private int boatSlot;

    private SeaskipperMapScreen() {
        if (McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            actualSeaskipperScreen = abstractContainerScreen;
        } else {
            throw new IllegalStateException(
                    "Tried to open custom seaskipper screen when normal seaskipper screen is not open");
        }

        generateSeaskipperPois();
    }

    public static Screen create() {
        return new SeaskipperMapScreen();
    }

    @Override
    public void onClose() {
        ContainerUtils.closeContainer(actualSeaskipperScreen.getMenu().containerId);
        super.onClose();
    }

    public void addDestination(SeaskipperDestinationItem destination, int slot) {
        destinations.put(destination, slot);
    }

    public void setBoatSlot(int slot) {
        boatSlot = slot;
    }

    @Override
    protected void doInit() {
        super.doInit();

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20 * 3,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_MANAGER_BUTTON,
                (b) -> toggleRoutes(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.translatable("screens.wynntils.seaskipperMapGui.showRoutes.name")),
                        Component.translatable("screens.wynntils.seaskipperMapGui.showRoutes.description")
                                .withStyle(ChatFormatting.GRAY))));

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20 * 2,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_ADD_BUTTON,
                (b) -> toggleDestinations(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.BLUE)
                                .append(Component.translatable(
                                        "screens.wynntils.seaskipperMapGui.showInaccessibleLocations.name")),
                        Component.translatable(
                                        "screens.wynntils.seaskipperMapGui.showInaccessibleLocations.description")
                                .withStyle(ChatFormatting.GRAY))));

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_OVERLAY_BUTTON,
                (b) -> toggleBorders(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.BLUE)
                                .append(Component.translatable("screens.wynntils.seaskipperMapGui.showBorders.name")),
                        Component.translatable("screens.wynntils.seaskipperMapGui.showBorders.description")
                                .withStyle(ChatFormatting.GRAY))));

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_BOAT_BUTTON,
                (b) -> buyBoat(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.translatable("screens.wynntils.seaskipperMapGui.buyBoat.name")),
                        Component.translatable("screens.wynntils.seaskipperMapGui.buyBoat.description")
                                .withStyle(ChatFormatting.GRAY))));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        updateMapCenterIfDragging(mouseX, mouseY);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();

        renderMap(
                poseStack,
                Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .renderUsingLinear
                        .get());

        RenderUtils.enableScissor(
                (int) (renderX + renderedBorderXOffset), (int) (renderY + renderedBorderYOffset), (int) mapWidth, (int)
                        mapHeight);

        renderPois(poseStack, mouseX, mouseY);

        renderCursor(
                poseStack,
                1.5f,
                Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .pointerColor
                        .get(),
                Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .pointerType
                        .get());

        RenderUtils.disableScissor();

        renderBackground(poseStack);

        renderCoordinates(poseStack, mouseX, mouseY);

        renderMapButtons(poseStack, mouseX, mouseY, partialTick);

        renderHoveredSeaskipperDestination(poseStack);
    }

    private void renderPois(PoseStack poseStack, int mouseX, int mouseY) {
        for (Poi poi : seaskipperPois) {
            SeaskipperPoi seaskipperPoi = (SeaskipperPoi) poi;

            if (seaskipperPoi.isPlayerInside()) {
                currentPoi = poi;
                break;
            }
        }

        if (renderAllDestinations) {
            renderPois(
                    seaskipperPois,
                    poseStack,
                    BoundingBox.centered(mapCenterX, mapCenterZ, width / currentZoom, height / currentZoom),
                    1,
                    mouseX,
                    mouseY);
        } else {
            availableDestinations = new ArrayList<>();

            for (SeaskipperDestinationItem destinationItem : destinations.keySet()) {
                String destination = destinationItem.getDestination();

                for (Poi poi : seaskipperPois) {
                    if (poi.getName().equals(destination)) {
                        availableDestinations.add(poi);
                        break;
                    }
                }
            }

            availableDestinations.add(currentPoi);

            renderPois(
                    availableDestinations,
                    poseStack,
                    BoundingBox.centered(mapCenterX, mapCenterZ, width / currentZoom, height / currentZoom),
                    1,
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderPois(
            List<Poi> pois,
            PoseStack poseStack,
            BoundingBox textureBoundingBox,
            float poiScale,
            int mouseX,
            int mouseY) {
        hovered = null;

        List<Poi> filteredPois = getRenderedPois(pois, textureBoundingBox, poiScale, mouseX, mouseY);

        if (renderRoutes) {
            for (Poi poi : availableDestinations) {
                if (!(poi instanceof SeaskipperPoi destination)) continue;

                float poiRenderX = MapRenderer.getRenderX(currentPoi, mapCenterX, centerX, currentZoom);
                float poiRenderZ = MapRenderer.getRenderZ(currentPoi, mapCenterZ, centerZ, currentZoom);

                float x = MapRenderer.getRenderX(destination, mapCenterX, centerX, currentZoom);
                float z = MapRenderer.getRenderZ(destination, mapCenterZ, centerZ, currentZoom);

                RenderUtils.drawLine(
                        poseStack, CommonColors.DARK_GRAY.withAlpha(0.5f), poiRenderX, poiRenderZ, x, z, 0, 1);
            }
        }

        MultiBufferSource.BufferSource bufferSource =
                McUtils.mc().renderBuffers().bufferSource();

        for (int i = filteredPois.size() - 1; i >= 0; i--) {
            Poi poi = filteredPois.get(i);

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

            if (hideTerritoryBorders) {
                SeaskipperPoi seaskipperPoi = (SeaskipperPoi) poi;
                seaskipperPoi.renderAtWithoutBorders(poseStack, bufferSource, poiRenderX, poiRenderZ, currentZoom);
            } else {
                poi.renderAt(poseStack, bufferSource, poiRenderX, poiRenderZ, hovered == poi, poiScale, currentZoom);
            }
        }

        bufferSource.endBatch();
    }

    private void renderHoveredSeaskipperDestination(PoseStack poseStack) {
        if (!(hovered instanceof SeaskipperPoi seaskipperPoi)) return;

        poseStack.pushPose();
        poseStack.translate(width - SCREEN_SIDE_OFFSET - 250, SCREEN_SIDE_OFFSET + 40, 101);

        final float centerHeight = 60;
        final int textureWidth = Texture.TERRITORY_TOOLTIP_CENTER.width();

        RenderUtils.drawTexturedRect(poseStack, Texture.TERRITORY_TOOLTIP_TOP, 0, 0);
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.TERRITORY_TOOLTIP_CENTER.resource(),
                0,
                Texture.TERRITORY_TOOLTIP_TOP.height(),
                textureWidth,
                centerHeight,
                textureWidth,
                Texture.TERRITORY_TOOLTIP_CENTER.height());
        RenderUtils.drawTexturedRect(
                poseStack, Texture.TERRITORY_NAME_BOX, 0, Texture.TERRITORY_TOOLTIP_TOP.height() + centerHeight);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        "Level %d".formatted(seaskipperPoi.getLevel()),
                        10,
                        10,
                        CommonColors.ORANGE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        float renderYOffset = 10;

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        "Starting Coordinates: %d, %d".formatted(seaskipperPoi.getStartX(), seaskipperPoi.getStartZ()),
                        10,
                        10 + renderYOffset,
                        CommonColors.YELLOW,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        renderYOffset += 10;

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        "Ending Coordinates: %d, %d".formatted(seaskipperPoi.getEndX(), seaskipperPoi.getEndZ()),
                        10,
                        10 + renderYOffset,
                        CommonColors.YELLOW,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        boolean isAccessible = false;
        boolean origin = false;
        int price = 0;

        for (SeaskipperDestinationItem destinationItem : destinations.keySet()) {
            String destination = destinationItem.getDestination();

            if (seaskipperPoi.getName().equals(destination)) {
                isAccessible = true;
                price = destinationItem.getPrice();
                break;
            }

            if (seaskipperPoi.getName().equals(currentPoi.getName())) {
                origin = true;
            }
        }

        if (isAccessible) {
            renderYOffset += 20;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            "Cost: %d²½".formatted(price),
                            10,
                            10 + renderYOffset,
                            CommonColors.GREEN,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);

            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            "Click to go here!",
                            10,
                            10 + renderYOffset,
                            CommonColors.BLUE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        } else if (origin) {
            renderYOffset += 30;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            "Origin",
                            10,
                            10 + renderYOffset,
                            CommonColors.RED,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        } else {
            renderYOffset += 30;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            "Inaccessible",
                            10,
                            10 + renderYOffset,
                            CommonColors.GRAY,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        }

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        seaskipperPoi.getName(),
                        7,
                        textureWidth,
                        Texture.TERRITORY_TOOLTIP_TOP.height() + centerHeight,
                        Texture.TERRITORY_TOOLTIP_TOP.height() + centerHeight + Texture.TERRITORY_NAME_BOX.height(),
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        for (Poi poi : seaskipperPois) {
            if (poi instanceof SeaskipperPoi destination) {
                if (destination.isSelected(mouseX, mouseY)) {
                    buyPass(destination.getName());

                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // Potentially a better way to do this
    private void generateSeaskipperPois() {
        seaskipperPois.add(new SeaskipperPoi("Bear Zoo Island", 15, -409, -2539, -283, -2414));
        seaskipperPois.add(new SeaskipperPoi("Dead Island", 70, 745, -4040, 1000, -3810));
        seaskipperPois.add(new SeaskipperPoi("Durum Isles", 20, 347, -2988, 595, -2791));
        seaskipperPois.add(new SeaskipperPoi("Galleon's Graveyard", 60, -688, -3615, -472, -3385));
        seaskipperPois.add(new SeaskipperPoi("Half Moon Island", 30, 900, -2660, 1100, -2480));
        seaskipperPois.add(new SeaskipperPoi("Jofash Docks", 90, 1178, -4175, 1445, -4012));
        seaskipperPois.add(new SeaskipperPoi("Llevigar", 40, -2048, -4403, -1910, -4206));
        seaskipperPois.add(new SeaskipperPoi("Mage Island", 30, 805, -2960, 983, -2787));
        seaskipperPois.add(new SeaskipperPoi("Maro Peaks", 60, -41, -4174, 453, -3788));
        seaskipperPois.add(new SeaskipperPoi("Nemract", 20, 10, -2300, 210, -2070));
        seaskipperPois.add(new SeaskipperPoi("Nesaak", 40, 20, -960, 100, -880));
        seaskipperPois.add(new SeaskipperPoi("Nodguj Island", 45, 695, -3470, 917, -3210));
        seaskipperPois.add(new SeaskipperPoi("Pirate Cove", 60, -750, -3251, -580, -3006));
        seaskipperPois.add(new SeaskipperPoi("Rooster Island", 20, -128, -2538, -30, -2448));
        seaskipperPois.add(new SeaskipperPoi("Selchar", 25, -100, -3290, 210, -3046));
        seaskipperPois.add(new SeaskipperPoi("Skiens Island", 55, 297, -3739, 577, -3367));
        seaskipperPois.add(new SeaskipperPoi("Volcanic Isles", 55, -1164, -3870, -722, -3530));
        seaskipperPois.add(new SeaskipperPoi("Zhight Island", 55, -727, -2990, -440, -2629));
    }

    private void toggleBorders() {
        hideTerritoryBorders = !hideTerritoryBorders;
    }

    private void toggleDestinations() {
        renderAllDestinations = !renderAllDestinations;
    }

    private void toggleRoutes() {
        renderRoutes = !renderRoutes;
    }

    private void buyBoat() {
        ContainerUtils.clickOnSlot(
                boatSlot,
                actualSeaskipperScreen.getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                actualSeaskipperScreen.getMenu().getItems());
    }

    private void buyPass(String destinationToTravelTo) {
        int passSlot = -1;

        for (Map.Entry<SeaskipperDestinationItem, Integer> entry : destinations.entrySet()) {
            String destination = entry.getKey().getDestination();

            if (destination.equals(destinationToTravelTo)) {
                passSlot = entry.getValue();
                break;
            }
        }

        if (passSlot == -1) {
            return;
        }

        ContainerUtils.clickOnSlot(
                passSlot,
                actualSeaskipperScreen.getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                actualSeaskipperScreen.getMenu().getItems());
    }
}
