/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.GuildMapFeature;
import com.wynntils.models.territories.TerritoryInfo;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.screens.maps.widgets.MapButton;
import com.wynntils.services.map.type.TerritoryFilterType;
import com.wynntils.services.mapdata.features.builtin.TerritoryArea;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.features.type.MapLocation;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class GuildMapScreen extends AbstractMapScreen {
    private boolean resourceMode = false;
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

    private GuildMapScreen() {
        super();
        centerMapAroundPlayer();
    }

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
                (b) -> McUtils.mc().setScreen(MainMapScreen.create(mapCenterX, mapCenterZ, zoomLevel)),
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
        PoseStack poseStack = guiGraphics.pose();

        if (holdingMapKey
                && !Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .openGuildMapKeybind
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

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        renderCoordinates(poseStack, mouseX, mouseY);

        renderMapButtons(guiGraphics, mouseX, mouseY, partialTick);

        renderZoomWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderHoveredTerritoryInfo(poseStack);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected Stream<MapFeature> getRenderedMapFeatures() {
        // FIXME: Add back hybrid/advancement mode
        // FIXME: Add territory connection map paths
        // FIXME: Add user markers
        return Services.MapData.getFeaturesForCategory("wynntils:territory")
                .filter(f -> f instanceof TerritoryArea)
                .map(f -> (TerritoryArea) f)
                .filter(this::filterDefense)
                .filter(this::filterTreasury)
                .map(f -> f);
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

        // Manage on shift right click
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                && KeyboardUtils.isShiftDown()
                && hoveredFeature instanceof TerritoryArea territoryArea) {
            Handlers.Command.queueCommand(
                    "gu territory " + territoryArea.getTerritoryProfile().getName());
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (hoveredFeature instanceof MapLocation mapLocation
                    && Services.UserMarker.isFeatureMarked(hoveredFeature)) {
                Services.UserMarker.removeMarkerAtLocation(mapLocation.getLocation());
                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            setCompassToMouseCoords(mouseX, mouseY, !KeyboardUtils.isShiftDown());
            return true;
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    private void renderHoveredTerritoryInfo(PoseStack poseStack) {
        if (!(hoveredFeature instanceof TerritoryArea territoryArea)) return;

        poseStack.pushPose();
        poseStack.translate(width - SCREEN_SIDE_OFFSET - 250, SCREEN_SIDE_OFFSET + 40, 101);

        if (territoryArea.isTerritoryProfileOutdated()) {
            renderTerritoryTooltipWithFakeInfo(poseStack, territoryArea);
        } else {
            renderTerritoryTooltip(poseStack, territoryArea);
        }

        poseStack.popPose();
    }

    public boolean isResourceMode() {
        return resourceMode;
    }

    private boolean filterDefense(TerritoryArea territoryArea) {
        return !territoryDefenseFilterEnabled
                || filterTerritory(
                        territoryArea,
                        territoryDefenseFilterType,
                        territoryDefenseFilterLevel,
                        area -> area.getTerritoryInfo()
                                .map(TerritoryInfo::getDefences)
                                .orElse(null));
    }

    private boolean filterTreasury(TerritoryArea territoryArea) {
        return !territoryTreasuryFilterEnabled
                || filterTerritory(
                        territoryArea,
                        territoryTreasuryFilterType,
                        territoryTreasuryFilterLevel,
                        area -> area.getTerritoryInfo()
                                .map(TerritoryInfo::getTreasury)
                                .orElse(null));
    }

    private boolean filterTerritory(
            TerritoryArea territoryArea,
            TerritoryFilterType filterType,
            GuildResourceValues filterLevel,
            Function<TerritoryArea, GuildResourceValues> getter) {
        GuildResourceValues guildResourceValue = getter.apply(territoryArea);
        if (guildResourceValue == null) return false;

        return switch (filterType) {
            case HIGHER -> guildResourceValue.getLevel() >= filterLevel.getLevel();
            case LOWER -> guildResourceValue.getLevel() <= filterLevel.getLevel();
            case DEFAULT -> guildResourceValue.getLevel() == filterLevel.getLevel();
        };
    }

    private static void renderTerritoryTooltip(PoseStack poseStack, TerritoryArea territoryArea) {
        Optional<TerritoryInfo> territoryInfoOpt = territoryArea.getTerritoryInfo();

        if (territoryInfoOpt.isEmpty()) {
            WynntilsMod.warn("TerritoryInfo is empty for %s, when it should not be."
                    .formatted(territoryArea.getTerritoryProfile().getName()));
            return;
        }

        final TerritoryInfo territoryInfo = territoryInfoOpt.get();
        final TerritoryProfile territoryProfile = territoryArea.getTerritoryProfile();

        final int textureWidth = Texture.MAP_INFO_TOOLTIP_CENTER.width();

        final float centerHeight = 75
                + (territoryInfo.getStorage().size()
                                + territoryInfo.getGenerators().size())
                        * 10
                + (territoryInfo.isHeadquarters() ? 20 : 0);

        RenderUtils.drawTexturedRect(poseStack, Texture.MAP_INFO_TOOLTIP_TOP, 0, 0);
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.MAP_INFO_TOOLTIP_CENTER.resource(),
                0,
                Texture.MAP_INFO_TOOLTIP_TOP.height(),
                textureWidth,
                centerHeight,
                textureWidth,
                Texture.MAP_INFO_TOOLTIP_CENTER.height());
        RenderUtils.drawTexturedRect(
                poseStack, Texture.MAP_INFO_NAME_BOX, 0, Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight);

        // guild
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(
                                "%s [%s]".formatted(territoryInfo.getGuildName(), territoryInfo.getGuildPrefix())),
                        10,
                        10,
                        CommonColors.MAGENTA,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        float renderYOffset = 20;

        for (GuildResource value : GuildResource.values()) {
            int generation = territoryInfo.getGeneration(value);
            CappedValue storage = territoryInfo.getStorage(value);

            if (generation != 0) {
                StyledText formattedGenerated = StyledText.fromString(
                        "%s+%d %s per Hour".formatted(value.getPrettySymbol(), generation, value.getName()));

                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                formattedGenerated,
                                10,
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
                                poseStack,
                                formattedStored,
                                10,
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
                        poseStack,
                        treasury,
                        10,
                        10 + renderYOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);
        renderYOffset += 10;
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        defences,
                        10,
                        10 + renderYOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        if (territoryInfo.isHeadquarters()) {
            renderYOffset += 20;
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("Guild Headquarters"),
                            10,
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
                        poseStack,
                        StyledText.fromString(ChatFormatting.GRAY + "Time Held: " + timeHeldString),
                        10,
                        10 + renderYOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        // Territory name
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(territoryProfile.getName()),
                        7,
                        textureWidth,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight + Texture.MAP_INFO_NAME_BOX.height(),
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }

    private static void renderTerritoryTooltipWithFakeInfo(PoseStack poseStack, TerritoryArea territoryArea) {
        final TerritoryProfile territoryProfile = territoryArea.getTerritoryProfile();

        final int textureWidth = Texture.MAP_INFO_TOOLTIP_CENTER.width();

        final float centerHeight = 35;

        RenderUtils.drawTexturedRect(poseStack, Texture.MAP_INFO_TOOLTIP_TOP, 0, 0);
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.MAP_INFO_TOOLTIP_CENTER.resource(),
                0,
                Texture.MAP_INFO_TOOLTIP_TOP.height(),
                textureWidth,
                centerHeight,
                textureWidth,
                Texture.MAP_INFO_TOOLTIP_CENTER.height());
        RenderUtils.drawTexturedRect(
                poseStack, Texture.MAP_INFO_NAME_BOX, 0, Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight);

        // guild
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(
                                "%s [%s]".formatted(territoryProfile.getGuild(), territoryProfile.getGuildPrefix())),
                        10,
                        10,
                        CommonColors.MAGENTA,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.guildMap.hybridMode.noAdvancementData")),
                        10,
                        30,
                        CommonColors.LIGHT_GRAY,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        // Territory name
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(territoryProfile.getName()),
                        7,
                        textureWidth,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight + Texture.MAP_INFO_NAME_BOX.height(),
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
