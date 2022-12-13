/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.features.user.map.GuildMapFeature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.MapRenderer;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.widgets.BasicTexturedButton;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.BoundingBox;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.wynn.model.map.TerritoryDefenseLevel;
import com.wynntils.wynn.model.map.poi.Poi;
import com.wynntils.wynn.model.map.poi.TerritoryPoi;
import com.wynntils.wynn.model.territory.TerritoryManager;
import com.wynntils.wynn.model.territory.objects.GuildResource;
import com.wynntils.wynn.model.territory.objects.TerritoryInfo;
import com.wynntils.wynn.model.territory.objects.TerritoryStorage;
import com.wynntils.wynn.objects.profiles.TerritoryProfile;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class GuildMapScreen extends AbstractMapScreen {
    private boolean resourceMode = false;
    private TerritoryDefenseLevel territoryDefenseFilterLevel = TerritoryDefenseLevel.OFF;

    private BasicTexturedButton territoryDefenseFilterButton;

    @Override
    protected void init() {
        super.init();

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
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(new TranslatableComponent("screens.wynntils.map.help.name")),
                        new TextComponent("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(new TranslatableComponent("screens.wynntils.guildMap.help.description1")))));

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_ADD_BUTTON,
                (b) -> resourceMode = !resourceMode,
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TranslatableComponent(
                                        "screens.wynntils.guildMap.toggleResourceColor.name")),
                        new TranslatableComponent("screens.wynntils.guildMap.toggleResourceColor.description")
                                .withStyle(ChatFormatting.GRAY))));

        territoryDefenseFilterButton = this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_ADD_BUTTON, // TODO: Add new cycle texture
                (b) -> {
                    // Left and right clicks cycle through the defense levels, middle click resets to OFF
                    territoryDefenseFilterLevel = (b == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
                            ? TerritoryDefenseLevel.OFF
                            : territoryDefenseFilterLevel.next();
                    territoryDefenseFilterButton.setTooltip(
                            territoryDefenseFilterLevel.getTerritoryDefenseFilterButtonTooltip());
                },
                territoryDefenseFilterLevel.getTerritoryDefenseFilterButtonTooltip()));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (holdingMapKey
                && !GuildMapFeature.INSTANCE.openGuildMapKeybind.getKeyMapping().isDown()) {
            this.onClose();
            return;
        }

        updateMapCenterIfDragging(mouseX, mouseY);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();

        renderMap(poseStack, GuildMapFeature.INSTANCE.renderUsingLinear);

        RenderUtils.enableScissor(
                (int) (renderX + renderedBorderXOffset), (int) (renderY + renderedBorderYOffset), (int) mapWidth, (int)
                        mapHeight);

        renderPois(poseStack, mouseX, mouseY);

        renderCursor(poseStack, 1.5f, GuildMapFeature.INSTANCE.pointerColor, GuildMapFeature.INSTANCE.pointerType);

        RenderSystem.disableScissor();

        renderBackground(poseStack);

        renderCoordinates(poseStack, mouseX, mouseY);

        renderMapButtons(poseStack, mouseX, mouseY, partialTick);

        renderHoveredTerritoryInfo(poseStack);
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

        if (territoryDefenseFilterLevel != TerritoryDefenseLevel.OFF) {
            filteredPois.removeIf( // Remove territories that do not match the filtered defense level
                    poi -> {
                        // Do not filter anything if the filter is off or if the poi is not a territory (shouldn't
                        // happen)
                        if (!(poi instanceof TerritoryPoi)) return false;

                        return !territoryDefenseFilterLevel
                                .asColoredString()
                                .equals(((TerritoryPoi) poi)
                                        .getTerritoryInfo()
                                        .getDefences()
                                        .asColoredString());
                    });
        }

        // Render trading routes
        // We render them in both directions because optimizing it is not cheap either
        for (Poi poi : filteredPois) {
            if (!(poi instanceof TerritoryPoi territoryPoi)) continue;

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

            for (String tradingRoute : territoryPoi.getTerritoryInfo().getTradingRoutes()) {
                TerritoryPoi routePoi = TerritoryManager.getTerritoryPoiFromAdvancement(tradingRoute);
                if (routePoi != null) {
                    float x = MapRenderer.getRenderX(routePoi, mapCenterX, centerX, currentZoom);
                    float z = MapRenderer.getRenderZ(routePoi, mapCenterZ, centerZ, currentZoom);

                    RenderUtils.drawLine(poseStack, CommonColors.DARK_GRAY, poiRenderX, poiRenderZ, x, z, 0, 1);
                }
            }
        }

        // Reverse and Render
        for (int i = filteredPois.size() - 1; i >= 0; i--) {
            Poi poi = filteredPois.get(i);

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

            poi.renderAt(poseStack, poiRenderX, poiRenderZ, hovered == poi, poiScale, currentZoom);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Manage on shift right click
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                && KeyboardUtils.isShiftDown()
                && hovered instanceof TerritoryPoi territoryPoi) {
            McUtils.player().chat("/gu territory " + territoryPoi.getName());
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderHoveredTerritoryInfo(PoseStack poseStack) {
        if (!(hovered instanceof TerritoryPoi territoryPoi)) return;

        poseStack.pushPose();
        poseStack.translate(width - SCREEN_SIDE_OFFSET - 250, SCREEN_SIDE_OFFSET + 40, 101);

        final TerritoryInfo territoryInfo = territoryPoi.getTerritoryInfo();
        final TerritoryProfile territoryProfile = territoryPoi.getTerritoryProfile();
        final float centerHeight = 55
                + (territoryInfo.getStorage().values().size()
                                + territoryInfo.getGenerators().size())
                        * 10
                + (territoryInfo.isHeadquarters() ? 20 : 0);
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

        // guild
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        "%s [%s]".formatted(territoryProfile.getGuild(), territoryProfile.getGuildPrefix()),
                        10,
                        10,
                        CommonColors.MAGENTA,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.OUTLINE);

        float renderYOffset = 20;

        for (GuildResource value : GuildResource.values()) {
            int generation = territoryInfo.getGeneration(value);
            TerritoryStorage storage = territoryInfo.getStorage(value);

            if (generation != 0) {
                String formattedGenerated =
                        "%s+%d %s per Hour".formatted(value.getPrettySymbol(), generation, value.getName());

                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                formattedGenerated,
                                10,
                                10 + renderYOffset,
                                CommonColors.WHITE,
                                HorizontalAlignment.Left,
                                VerticalAlignment.Top,
                                FontRenderer.TextShadow.OUTLINE);
                renderYOffset += 10;
            }

            if (storage != null) {
                String formattedStored = "%s%d/%d %s stored"
                        .formatted(value.getPrettySymbol(), storage.current(), storage.max(), value.getName());

                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                formattedStored,
                                10,
                                10 + renderYOffset,
                                CommonColors.WHITE,
                                HorizontalAlignment.Left,
                                VerticalAlignment.Top,
                                FontRenderer.TextShadow.OUTLINE);
                renderYOffset += 10;
            }
        }

        renderYOffset += 10;

        String treasury = ChatFormatting.GRAY
                + "✦ Treasury: %s".formatted(territoryInfo.getTreasury().asColoredString());
        String defences = ChatFormatting.GRAY
                + "Territory Defences: %s".formatted(territoryInfo.getDefences().asColoredString());

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        treasury,
                        10,
                        10 + renderYOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.OUTLINE);
        renderYOffset += 10;
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        defences,
                        10,
                        10 + renderYOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.OUTLINE);

        if (territoryInfo.isHeadquarters()) {
            renderYOffset += 20;
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            "Guild Headquarters",
                            10,
                            10 + renderYOffset,
                            CommonColors.RED,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.OUTLINE);
        }

        // Territory name
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        territoryPoi.getName(),
                        7,
                        textureWidth,
                        Texture.TERRITORY_TOOLTIP_TOP.height() + centerHeight,
                        Texture.TERRITORY_TOOLTIP_TOP.height() + centerHeight + Texture.TERRITORY_NAME_BOX.height(),
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.OUTLINE);

        poseStack.popPose();
    }

    private void renderPois(PoseStack poseStack, int mouseX, int mouseY) {
        List<Poi> pois = TerritoryManager.getTerritoryPoisFromAdvancement();

        renderPois(
                pois,
                poseStack,
                BoundingBox.centered(mapCenterX, mapCenterZ, width / currentZoom, height / currentZoom),
                1,
                mouseX,
                mouseY);
    }

    public boolean isResourceMode() {
        return resourceMode;
    }
}
