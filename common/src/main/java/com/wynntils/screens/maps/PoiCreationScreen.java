/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.maps.widgets.IconButton;
import com.wynntils.services.mapdata.MapFeatureRenderer;
import com.wynntils.services.mapdata.attributes.FixedMapVisibility;
import com.wynntils.services.mapdata.attributes.type.MapIcon;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.features.WaypointLocation;
import com.wynntils.services.mapdata.providers.builtin.WaypointsProvider;
import com.wynntils.services.mapdata.providers.json.JsonMapAttributes;
import com.wynntils.services.mapdata.providers.json.JsonMapAttributesBuilder;
import com.wynntils.services.mapdata.providers.json.JsonMapVisibility;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class PoiCreationScreen extends AbstractMapScreen {
    // Constants
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("[-+]?\\d{1,8}");
    private static final float GRID_DIVISIONS = 64.0f;
    private static final int ICONS_PER_PAGE = 5;

    // Collections
    private final List<MapIcon> availableIcons = new ArrayList<>();
    private final List<IconButton> iconButtons = new ArrayList<>();
    private final List<VisibilitySlider> labelSliders = new ArrayList<>();
    private final List<VisibilitySlider> iconSliders = new ArrayList<>();

    // Widgets
    private Button addCustomIconButton;
    private Button previousIconButton;
    private Button centerOnPlayerButton;
    private Button centerOnWorldButton;
    private Button nextIconButton;
    private Button saveButton;
    private Button tabButton;
    private OptionButton labelShadowButton;
    private OptionButton iconTypeButton;
    private OptionButton labelVisiblityButton;
    private OptionButton iconVisiblityButton;
    private TextInputBoxWidget labelInput;
    private TextInputBoxWidget labelColorInput;
    private TextInputBoxWidget iconColorInput;
    private TextInputBoxWidget xInput;
    private TextInputBoxWidget yInput;
    private TextInputBoxWidget zInput;
    private TextInputBoxWidget priorityInput;
    private TextInputBoxWidget focusedTextInput;
    private VisibilitySlider labelMinVisibilitySlider;
    private VisibilitySlider labelMaxVisibilitySlider;
    private VisibilitySlider labelFadeSlider;
    private VisibilitySlider iconMinVisibilitySlider;
    private VisibilitySlider iconMaxVisibilitySlider;
    private VisibilitySlider iconFadeSlider;

    // UI Size, positions etc
    private float dividedWidth;
    private float dividedHeight;

    // Screen information
    private final Screen returnScreen;
    private boolean firstSetup;
    private boolean visibilityTab = false;
    private int iconScrollOffset = 0;
    private Location setupLocation;
    private WaypointsProvider.WaypointLocation oldWaypoint;

    // Waypoint details
    private CustomColor iconColorCache = CommonColors.WHITE;
    private CustomColor labelColorCache = CommonColors.WHITE;
    private IconType iconType = IconType.WYNNTILS;
    private int selectedIconIndex = 0;
    private Integer parsedXInput;
    private Integer parsedYInput = 0;
    private Integer parsedZInput;
    private int priority = 500;
    private String category = "";
    private String iconId = "";
    private String label = "";
    private TextShadow labelShadow = TextShadow.NORMAL;
    private VisibilityType iconVisibilityType = VisibilityType.CUSTOM;
    private VisibilityType labelVisibilityType = VisibilityType.CUSTOM;
    private WaypointsProvider.WaypointLocation waypoint;

    private PoiCreationScreen(MainMapScreen oldMapScreen) {
        super();
        this.returnScreen = oldMapScreen;

        this.firstSetup = true;
    }

    private PoiCreationScreen(MainMapScreen oldMapScreen, Location setupLocation) {
        this(oldMapScreen);

        this.setupLocation = setupLocation;
        this.firstSetup = true;
    }

    private PoiCreationScreen(MainMapScreen oldMapScreen, WaypointsProvider.WaypointLocation oldWaypoint) {
        this(oldMapScreen);

        this.oldWaypoint = oldWaypoint;
        this.firstSetup = true;
    }

    private PoiCreationScreen(PoiManagementScreen managementScreen, WaypointsProvider.WaypointLocation oldWaypoint) {
        super();
        this.returnScreen = managementScreen;

        this.oldWaypoint = oldWaypoint;
        this.firstSetup = true;
    }

    public static Screen create(MainMapScreen oldMapScreen) {
        return new PoiCreationScreen(oldMapScreen);
    }

    public static Screen create(MainMapScreen oldMapScreen, Location setupLocation) {
        return new PoiCreationScreen(oldMapScreen, setupLocation);
    }

    public static Screen create(MainMapScreen oldMapScreen, WaypointsProvider.WaypointLocation oldWaypoint) {
        return new PoiCreationScreen(oldMapScreen, oldWaypoint);
    }

    public static Screen create(PoiManagementScreen managementScreen, WaypointsProvider.WaypointLocation oldWaypoint) {
        return new PoiCreationScreen(managementScreen, oldWaypoint);
    }

    @Override
    protected void doInit() {
        dividedWidth = this.width / GRID_DIVISIONS;
        dividedHeight = this.height / GRID_DIVISIONS;

        renderX = dividedWidth * 32;
        renderWidth = dividedWidth * 29;
        renderY = dividedHeight * 5;
        renderHeight = dividedHeight * 54;

        float borderScaleX = (float) this.width / Texture.FULLSCREEN_MAP_BORDER.width();
        float borderScaleY = (float) this.height / Texture.FULLSCREEN_MAP_BORDER.height();

        renderedBorderXOffset = 3 * borderScaleX;
        renderedBorderYOffset = 3 * borderScaleY;

        mapWidth = renderWidth - renderedBorderXOffset * 2f + 1; // +1 to fix rounding causing black line on the right
        centerX = renderX + renderedBorderXOffset + mapWidth / 2f;
        mapHeight = renderHeight - renderedBorderYOffset * 2f;
        centerZ = renderY + renderedBorderYOffset + mapHeight / 2f;

        // region Label
        labelInput = new TextInputBoxWidget(
                (int) dividedWidth,
                (int) (dividedHeight * 14),
                (int) (dividedWidth * 10),
                20,
                (s) -> {
                    label = s;
                    updateWaypoint();
                },
                this,
                labelInput);
        this.addRenderableWidget(labelInput);

        if (firstSetup && oldWaypoint != null) {
            if (oldWaypoint.getAttributes().isPresent()) {
                oldWaypoint
                        .getAttributes()
                        .get()
                        .getLabel()
                        .ifPresent(oldLabel -> labelInput.setTextBoxInput(oldLabel));
            }
        }

        if (firstSetup) {
            setFocusedTextInput(labelInput);
        }

        if (firstSetup && oldWaypoint != null) {
            if (oldWaypoint.getAttributes().isPresent()) {
                oldWaypoint.getAttributes().get().getLabelShadow().ifPresent(shadow -> labelShadow = shadow);
            }
        }

        labelShadowButton = new OptionButton(
                (int) (dividedWidth * 12),
                (int) (dividedHeight * 14),
                (int) (dividedWidth * 10),
                Component.literal(labelShadow.name()));
        this.addRenderableWidget(labelShadowButton);

        labelColorInput = new TextInputBoxWidget(
                (int) (dividedWidth * 23),
                (int) (dividedHeight * 14),
                (int) (dividedWidth * 5.5),
                20,
                (s) -> {
                    CustomColor color = CustomColor.fromHexString(s);

                    if (color == CustomColor.NONE) {
                        // Default to white
                        labelColorCache = CommonColors.WHITE;
                        labelColorInput.setRenderColor(CommonColors.RED);
                    } else {
                        labelColorCache = color;
                        labelColorInput.setRenderColor(CommonColors.GREEN);
                    }

                    updateWaypoint();
                },
                this,
                labelColorInput);
        this.addRenderableWidget(labelColorInput);

        if (firstSetup && oldWaypoint != null && oldWaypoint.getAttributes().isPresent()) {
            oldWaypoint
                    .getAttributes()
                    .get()
                    .getLabelColor()
                    .ifPresent(labelColor -> labelColorInput.setTextBoxInput(labelColor.toHexString()));
        }

        if (labelColorInput.getTextBoxInput().isEmpty()) {
            labelColorInput.setTextBoxInput("#FFFFFF");
        }
        // endregion

        // region Icon
        if (firstSetup) {
            if (oldWaypoint != null && oldWaypoint.getAttributes().isPresent()) {
                if (oldWaypoint.getAttributes().get().getIconId().isPresent()) {
                    String oldIconId =
                            oldWaypoint.getAttributes().get().getIconId().get();

                    if (oldIconId.equals(MapIcon.NO_ICON_ID)) {
                        iconType = IconType.NONE;
                    } else if (oldIconId.startsWith("wynntils:icon:personal:")) {
                        iconType = IconType.CUSTOM;
                    } else {
                        Optional<MapIcon> oldMapIcon = Services.MapData.MAP_ICONS_PROVIDER
                                .getIcons()
                                .filter(mapIcon -> mapIcon.getIconId().equals(oldIconId))
                                .findFirst();

                        oldMapIcon.ifPresent(oldIcon -> selectedIconIndex = Services.MapData.MAP_ICONS_PROVIDER
                                .getIcons()
                                .toList()
                                .indexOf(oldMapIcon.get()));

                        iconScrollOffset = selectedIconIndex;

                        availableIcons.addAll(
                                Services.MapData.MAP_ICONS_PROVIDER.getIcons().toList());
                    }

                    iconId = oldIconId;
                } else {
                    iconType = IconType.NONE;
                }
            } else {
                availableIcons.addAll(
                        Services.MapData.MAP_ICONS_PROVIDER.getIcons().toList());
            }

            updateIcon();
        }

        iconTypeButton = new OptionButton(
                (int) dividedWidth,
                (int) (dividedHeight * 25),
                (int) (dividedWidth * 6),
                Component.literal(iconType.name()));
        this.addRenderableWidget(iconTypeButton);

        previousIconButton = new Button.Builder(Component.literal("<"), (button) -> {
                    if (iconScrollOffset - 1 < 0) {
                        iconScrollOffset = availableIcons.size() - 1;
                    } else {
                        iconScrollOffset--;
                    }

                    populateIcons();
                })
                .pos((int) (dividedWidth * 8), (int) (dividedHeight * 25))
                .size(20, 20)
                .build();
        this.addRenderableWidget(previousIconButton);

        nextIconButton = new Button.Builder(Component.literal(">"), (button) -> {
                    if (iconScrollOffset + 1 >= availableIcons.size()) {
                        iconScrollOffset = 0;
                    } else {
                        iconScrollOffset++;
                    }

                    populateIcons();
                })
                .pos((int) (dividedWidth * 20), (int) (dividedHeight * 25))
                .size(20, 20)
                .build();
        this.addRenderableWidget(nextIconButton);

        addCustomIconButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiCreation.addCustomIcon"),
                        (button) -> McUtils.mc().setScreen(CustomWaypointIconScreen.create(this)))
                .pos((int) (dividedWidth * 8) + 20, (int) (dividedHeight * 25) + 20)
                .size((int) (dividedWidth * 10), 20)
                .build();
        this.addRenderableWidget(addCustomIconButton);

        addCustomIconButton.visible = iconType == IconType.CUSTOM;

        iconColorInput = new TextInputBoxWidget(
                (int) (dividedWidth * 23),
                (int) (dividedHeight * 25),
                (int) (dividedWidth * 5.5),
                20,
                (s) -> {
                    CustomColor color = CustomColor.fromHexString(s);

                    if (color == CustomColor.NONE) {
                        // Default to white
                        iconColorCache = CommonColors.WHITE;
                        iconColorInput.setRenderColor(CommonColors.RED);
                    } else {
                        iconColorCache = color;
                        iconColorInput.setRenderColor(CommonColors.GREEN);
                    }

                    updateWaypoint();
                },
                this,
                iconColorInput);
        this.addRenderableWidget(iconColorInput);

        if (firstSetup && oldWaypoint != null && oldWaypoint.getAttributes().isPresent()) {
            oldWaypoint
                    .getAttributes()
                    .get()
                    .getIconColor()
                    .ifPresent(iconColor -> iconColorInput.setTextBoxInput(iconColor.toHexString()));
        }

        if (iconColorInput.getTextBoxInput().isEmpty()) {
            iconColorInput.setTextBoxInput("#FFFFFF");
        }
        // endregion

        // region Location
        xInput = new TextInputBoxWidget(
                (int) dividedWidth,
                (int) (dividedHeight * 36),
                (int) (dividedWidth * 7),
                20,
                s -> {
                    if (COORDINATE_PATTERN.matcher(s).matches()) {
                        parsedXInput = Integer.parseInt(s);
                        xInput.setRenderColor(CommonColors.GREEN);
                        if (parsedZInput != null) {
                            updateMapCenter(parsedXInput, parsedZInput);
                        }
                    } else {
                        parsedXInput = null;
                        xInput.setRenderColor(CommonColors.RED);
                    }
                    updateWaypoint();
                },
                this,
                xInput);
        this.addRenderableWidget(xInput);

        yInput = new TextInputBoxWidget(
                (int) (dividedWidth * 9),
                (int) (dividedHeight * 36),
                (int) (dividedWidth * 7),
                20,
                s -> {
                    if (COORDINATE_PATTERN.matcher(s).matches()) {
                        parsedYInput = Integer.parseInt(s);
                        yInput.setRenderColor(CommonColors.GREEN);
                    } else {
                        parsedYInput = 0;
                    }

                    updateWaypoint();
                },
                this,
                yInput);
        this.addRenderableWidget(yInput);

        zInput = new TextInputBoxWidget(
                (int) (dividedWidth * 17),
                (int) (dividedHeight * 36),
                (int) (dividedWidth * 7),
                20,
                s -> {
                    if (COORDINATE_PATTERN.matcher(s).matches()) {
                        parsedZInput = Integer.parseInt(s);
                        zInput.setRenderColor(CommonColors.GREEN);
                        if (parsedXInput != null) {
                            updateMapCenter(parsedXInput, parsedZInput);
                        }
                    } else {
                        parsedZInput = null;
                        zInput.setRenderColor(CommonColors.RED);
                    }
                    updateWaypoint();
                },
                this,
                zInput);
        this.addRenderableWidget(zInput);

        if (firstSetup) {
            if (oldWaypoint != null) {
                xInput.setTextBoxInput(String.valueOf(oldWaypoint.getLocation().x));
                yInput.setTextBoxInput(String.valueOf(oldWaypoint.getLocation().y));
                zInput.setTextBoxInput(String.valueOf(oldWaypoint.getLocation().z));
            } else if (setupLocation != null) {
                xInput.setTextBoxInput(String.valueOf(setupLocation.x()));
                yInput.setTextBoxInput(String.valueOf(setupLocation.y()));
                zInput.setTextBoxInput(String.valueOf(setupLocation.z()));
            }
        }

        centerOnPlayerButton = new Button.Builder(Component.literal("🧍"), (button) -> {
                    xInput.setTextBoxInput(String.valueOf(McUtils.player().getBlockX()));
                    yInput.setTextBoxInput(String.valueOf(McUtils.player().getBlockY()));
                    zInput.setTextBoxInput(String.valueOf(McUtils.player().getBlockZ()));
                })
                .pos((int) (dividedWidth * 26), (int) (dividedHeight * 36))
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiCreation.centerPlayer")))
                .build();
        this.addRenderableWidget(centerOnPlayerButton);

        centerOnWorldButton = new Button.Builder(Component.literal("🌍"), (button) -> {
                    xInput.setTextBoxInput(String.valueOf(MAP_CENTER_X));
                    yInput.setTextBoxInput("0");
                    zInput.setTextBoxInput(String.valueOf(MAP_CENTER_Z));
                })
                .pos((int) (dividedWidth * 29), (int) (dividedHeight * 36))
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiCreation.centerWorld")))
                .build();
        this.addRenderableWidget(centerOnWorldButton);
        // endregion

        // region Visibility
        priorityInput = new TextInputBoxWidget(
                (int) dividedWidth,
                (int) (dividedHeight * 14),
                (int) (dividedWidth * 9),
                20,
                s -> {
                    try {
                        priority = Integer.parseInt(s);
                        priorityInput.setRenderColor(CommonColors.WHITE);
                    } catch (NumberFormatException e) {
                        priority = -1;
                        priorityInput.setRenderColor(CommonColors.RED);
                    }

                    if (priority < 0 || priority > 1000) {
                        priorityInput.setRenderColor(CommonColors.RED);
                    }

                    updateWaypoint();
                },
                this,
                priorityInput);
        this.addRenderableWidget(priorityInput);

        if (firstSetup) {
            if (oldWaypoint != null
                    && oldWaypoint.getAttributes().isPresent()
                    && oldWaypoint.getAttributes().get().getPriority().isPresent()) {
                priorityInput.setTextBoxInput(String.valueOf(
                        oldWaypoint.getAttributes().get().getPriority().get()));
            } else {
                priorityInput.setTextBoxInput(String.valueOf(priority));
            }
        }

        labelVisiblityButton = new OptionButton(
                (int) (dividedWidth * 11),
                (int) (dividedHeight * 14),
                (int) (dividedWidth * 9),
                Component.literal(labelVisibilityType.name()));
        this.addRenderableWidget(labelVisiblityButton);

        iconVisiblityButton = new OptionButton(
                (int) (dividedWidth * 21),
                (int) (dividedHeight * 14),
                (int) (dividedWidth * 9),
                Component.literal(iconVisibilityType.name()));
        this.addRenderableWidget(iconVisiblityButton);

        labelMinVisibilitySlider = new VisibilitySlider(
                (int) dividedWidth, (int) (dividedHeight * 25), (int) (dividedWidth * 9), Component.literal("1"), 0.0);
        this.addRenderableWidget(labelMinVisibilitySlider);

        labelMaxVisibilitySlider = new VisibilitySlider(
                (int) (dividedWidth * 11),
                (int) (dividedHeight * 25),
                (int) (dividedWidth * 9),
                Component.literal("100"),
                1.0);
        this.addRenderableWidget(labelMaxVisibilitySlider);

        labelFadeSlider = new VisibilitySlider(
                (int) (dividedWidth * 21),
                (int) (dividedHeight * 25),
                (int) (dividedWidth * 9),
                Component.literal("5"),
                0.05);
        this.addRenderableWidget(labelFadeSlider);

        iconMinVisibilitySlider = new VisibilitySlider(
                (int) dividedWidth, (int) (dividedHeight * 36), (int) (dividedWidth * 9), Component.literal("1"), 0.0);
        this.addRenderableWidget(iconMinVisibilitySlider);

        iconMaxVisibilitySlider = new VisibilitySlider(
                (int) (dividedWidth * 11),
                (int) (dividedHeight * 36),
                (int) (dividedWidth * 9),
                Component.literal("100"),
                1.0);
        this.addRenderableWidget(iconMaxVisibilitySlider);

        iconFadeSlider = new VisibilitySlider(
                (int) (dividedWidth * 21),
                (int) (dividedHeight * 36),
                (int) (dividedWidth * 9),
                Component.literal("5"),
                0.05);
        this.addRenderableWidget(iconFadeSlider);

        if (firstSetup && oldWaypoint != null) {
            if (oldWaypoint.getAttributes().isPresent()
                    && oldWaypoint.getAttributes().get().getLabelVisibility().isPresent()) {
                MapVisibility labelVisibility =
                        oldWaypoint.getAttributes().get().getLabelVisibility().get();

                labelVisibility.getMin().ifPresent(min -> labelMinVisibilitySlider.setVisibility(min));
                labelVisibility.getMax().ifPresent(max -> labelMaxVisibilitySlider.setVisibility(max));
                labelVisibility.getFade().ifPresent(fade -> labelFadeSlider.setVisibility(fade));
            }

            if (oldWaypoint.getAttributes().isPresent()
                    && oldWaypoint.getAttributes().get().getIconVisibility().isPresent()) {
                MapVisibility iconVisibility =
                        oldWaypoint.getAttributes().get().getIconVisibility().get();

                iconVisibility.getMin().ifPresent(min -> iconMinVisibilitySlider.setVisibility(min));
                iconVisibility.getMax().ifPresent(max -> iconMaxVisibilitySlider.setVisibility(max));
                iconVisibility.getFade().ifPresent(fade -> iconFadeSlider.setVisibility(fade));
            }
        }

        labelSliders.add(labelMinVisibilitySlider);
        labelSliders.add(labelMaxVisibilitySlider);
        labelSliders.add(labelFadeSlider);
        iconSliders.add(iconMinVisibilitySlider);
        iconSliders.add(iconMaxVisibilitySlider);
        iconSliders.add(iconFadeSlider);
        // endregion

        // region Category
        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.poiCreation.changeCategory"),
                        (button) -> McUtils.mc().setScreen(WaypointCategoryScreen.create(this, category)))
                .pos((int) (dividedWidth * 18), (int) (dividedHeight * 48))
                .size((int) (dividedWidth * 9), 20)
                .build());

        if (firstSetup && oldWaypoint != null) {
            String oldCategory = oldWaypoint.getCategoryId();

            if (oldCategory.equals("wynntils:personal:waypoint")) {
                category = "";
            } else {
                category = oldCategory.substring("wynntils:personal:waypoint:".length());
            }
        }
        // endregion

        // region Screen Interactions
        tabButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiCreation.editVisibility"), (button) -> {
                            visibilityTab = !visibilityTab;

                            if (visibilityTab) {
                                tabButton.setMessage(
                                        Component.translatable("screens.wynntils.poiCreation.editWaypoint"));
                            } else {
                                tabButton.setMessage(
                                        Component.translatable("screens.wynntils.poiCreation.editVisibility"));
                            }

                            toggleWidgets();
                        })
                .pos((int) (dividedWidth * 10), (int) (dividedHeight * 42))
                .size((int) (dividedWidth * 12), 20)
                .build();
        this.addRenderableWidget(tabButton);

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.poiCreation.cancel"), (button) -> this.onClose())
                .pos((int) (dividedWidth * 4), (int) (dividedHeight * 56))
                .size((int) (dividedWidth * 9), 20)
                .build());

        saveButton = new Button.Builder(Component.translatable("screens.wynntils.poiCreation.save"), (button) -> {
                    saveWaypoint();
                    this.onClose();
                })
                .pos((int) (dividedWidth * 17), (int) (dividedHeight * 56))
                .size((int) (dividedWidth * 9), 20)
                .build();
        this.addRenderableWidget(saveButton);
        // endregion

        if (iconType == IconType.WYNNTILS) {
            availableIcons.addAll(Services.MapData.MAP_ICONS_PROVIDER.getIcons().toList());
        } else if (iconType == IconType.CUSTOM) {
            availableIcons.addAll(Services.Waypoints.getCustomIcons());
        }

        toggleWidgets();
        updateWaypoint();
        populateIcons();
        firstSetup = false;

        previousIconButton.active = availableIcons.size() > ICONS_PER_PAGE;
        nextIconButton.active = availableIcons.size() > ICONS_PER_PAGE;
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderGradientBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();
        renderMap(poseStack);
        RenderUtils.enableScissor(
                (int) (renderX + renderedBorderXOffset), (int) (renderY + renderedBorderYOffset), (int) mapWidth, (int)
                        mapHeight);

        if (waypoint != null) {
            MapFeatureRenderer.renderMapFeature(
                    poseStack,
                    guiGraphics.bufferSource(),
                    waypoint,
                    Services.MapData.resolveMapAttributes(waypoint),
                    MapRenderer.getRenderX(waypoint.getLocation().x, mapCenterX, centerX, zoomRenderScale),
                    MapRenderer.getRenderZ(waypoint.getLocation().z, mapCenterZ, centerZ, zoomRenderScale),
                    false,
                    1,
                    zoomRenderScale,
                    true);
        }

        renderCursor(
                poseStack,
                1.5f,
                Managers.Feature.getFeatureInstance(MainMapFeature.class)
                        .pointerColor
                        .get(),
                Managers.Feature.getFeatureInstance(MainMapFeature.class)
                        .pointerType
                        .get());

        RenderUtils.disableScissor();

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        if (!visibilityTab) {
            // region Label
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.poiCreation.label") + ":"),
                            dividedWidth,
                            dividedHeight * 12.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.poiCreation.labelShadow") + ":"),
                            dividedWidth * 12,
                            dividedHeight * 12.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.poiCreation.labelColor") + ":"),
                            dividedWidth * 23,
                            dividedHeight * 12.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            RenderUtils.drawRect(poseStack, labelColorCache, dividedWidth * 29, dividedHeight * 14, 0, 20, 20);
            // endregion

            // region Icon
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.poiCreation.icon") + ":"),
                            dividedWidth,
                            dividedHeight * 23.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            if (iconType != IconType.NONE) {
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                StyledText.fromString(I18n.get("screens.wynntils.poiCreation.iconColor") + ":"),
                                dividedWidth * 23.0f,
                                dividedHeight * 23.5f,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL);

                RenderUtils.drawRect(poseStack, iconColorCache, dividedWidth * 29, dividedHeight * 25, 0, 20, 20);

                renderIcons(guiGraphics, mouseX, mouseY, partialTick);
            }
            // endregion

            // region Location
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("X:"),
                            dividedWidth,
                            dividedHeight * 34.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("Y:"),
                            dividedWidth * 9.0f,
                            dividedHeight * 34.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("Z:"),
                            dividedWidth * 17.0f,
                            dividedHeight * 34.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            // endregion
        } else {
            // region Visiblity
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.poiCreation.priority") + ":"),
                            dividedWidth,
                            dividedHeight * 12.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.poiCreation.labelVisibility") + ":"),
                            dividedWidth * 11.0f,
                            dividedHeight * 12.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            if (iconType != IconType.NONE) {
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                StyledText.fromString(I18n.get("screens.wynntils.poiCreation.iconVisibility") + ":"),
                                dividedWidth * 21.0f,
                                dividedHeight * 12.5f,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL);
            }

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.poiCreation.labelMinVisibility") + ":"),
                            dividedWidth,
                            dividedHeight * 23.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.poiCreation.labelMaxVisibility") + ":"),
                            dividedWidth * 11.0f,
                            dividedHeight * 23.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.poiCreation.labelFade") + ":"),
                            dividedWidth * 21.0f,
                            dividedHeight * 23.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            if (iconType != IconType.NONE) {
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                StyledText.fromString(I18n.get("screens.wynntils.poiCreation.iconMinVisibility") + ":"),
                                dividedWidth,
                                dividedHeight * 34.5f,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL);

                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                StyledText.fromString(I18n.get("screens.wynntils.poiCreation.iconMaxVisibility") + ":"),
                                dividedWidth * 11.0f,
                                dividedHeight * 34.5f,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL);

                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                StyledText.fromString(I18n.get("screens.wynntils.poiCreation.iconFade") + ":"),
                                dividedWidth * 21.0f,
                                dividedHeight * 34.5f,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL);
            }
            // endregion
        }

        FontRenderer.getInstance()
                .renderScrollingText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.poiCreation.currentCategory") + ": "
                                + (category.isEmpty() ? "DEFAULT" : category)),
                        dividedWidth * 2.0f,
                        dividedHeight * 48.0f + 10,
                        dividedWidth * 15.0f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(returnScreen);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (labelShadowButton != null && labelShadowButton.isMouseOver(mouseX, mouseY)) {
            handleLabelShadowClick(button);
            updateWaypoint();

            return true;
        } else if (iconTypeButton != null && iconTypeButton.isMouseOver(mouseX, mouseY)) {
            handleIconTypeClick(button);

            populateIcons();
            updateIcon();
            updateWaypoint();
            return true;
        } else if (labelVisiblityButton != null && labelVisiblityButton.isMouseOver(mouseX, mouseY)) {
            handleLabelVisibilityClick(button);

            updateWaypoint();
            return true;
        } else if (iconVisiblityButton != null && iconVisiblityButton.isMouseOver(mouseX, mouseY)) {
            handleIconVisibilityClick(button);

            updateWaypoint();
            return true;
        }

        for (VisibilitySlider slider : labelSliders) {
            if (slider.isMouseOver(mouseX, mouseY)) {
                labelVisibilityType = VisibilityType.CUSTOM;
                labelVisiblityButton.setMessage(Component.literal(labelVisibilityType.name()));
                break;
            }
        }

        if (visibilityTab && iconType != IconType.NONE) {
            for (VisibilitySlider slider : iconSliders) {
                if (slider.isMouseOver(mouseX, mouseY)) {
                    iconVisibilityType = VisibilityType.CUSTOM;
                    iconVisiblityButton.setMessage(Component.literal(iconVisibilityType.name()));
                    break;
                }
            }
        }

        for (IconButton iconButton : iconButtons) {
            if (iconButton.isMouseOver(mouseX, mouseY)) {
                return iconButton.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            int gameX = (int) ((mouseX - centerX) / zoomRenderScale + mapCenterX);
            int gameZ = (int) ((mouseY - centerZ) / zoomRenderScale + mapCenterZ);
            xInput.setTextBoxInput(String.valueOf(gameX));
            zInput.setTextBoxInput(String.valueOf(gameZ));
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return (focusedTextInput != null && focusedTextInput.charTyped(codePoint, modifiers))
                || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // When tab is pressed, focus the next text box
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            int index = focusedTextInput == null ? 0 : children().indexOf(focusedTextInput);
            int actualIndex = Math.max(index, 0) + 1;

            // Try to find next text input
            // From index - end
            for (int i = actualIndex; i < children().size(); i++) {
                if (children().get(i) instanceof TextInputBoxWidget textInputBoxWidget) {
                    setFocusedTextInput(textInputBoxWidget);
                    return true;
                }
            }

            // From 0 - index
            for (int i = 0; i < Math.min(actualIndex, children().size()); i++) {
                if (children().get(i) instanceof TextInputBoxWidget textInputBoxWidget) {
                    setFocusedTextInput(textInputBoxWidget);
                    return true;
                }
            }
        }

        return (focusedTextInput != null && focusedTextInput.keyPressed(keyCode, scanCode, modifiers))
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSelectedIcon(MapIcon selectedIcon) {
        selectedIconIndex = availableIcons.indexOf(selectedIcon);

        populateIcons();
        updateIcon();
        updateWaypoint();
    }

    public void setCustomIcon(int newIndex) {
        selectedIconIndex = newIndex;

        populateIcons();
        updateIcon();
        updateWaypoint();
    }

    private void renderIcons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (IconButton iconButton : iconButtons) {
            iconButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void updateIcon() {
        switch (iconType) {
            case WYNNTILS -> {
                iconId = Services.MapData.MAP_ICONS_PROVIDER
                        .getIcons()
                        .toList()
                        .get(selectedIconIndex)
                        .getIconId();
            }
            case CUSTOM -> {
                if (Services.Waypoints.getCustomIcons().isEmpty()) {
                    iconId = "";
                } else {
                    iconId = Services.Waypoints.getCustomIcons()
                            .get(selectedIconIndex)
                            .getIconId();
                }
            }
            default -> {
                iconId = MapIcon.NO_ICON_ID;
            }
        }
    }

    private void updateWaypoint() {
        if (saveButton == null) return;
        if (parsedXInput == null || parsedYInput == null || parsedZInput == null) {
            saveButton.active = false;
            return;
        }

        Location location = new Location(parsedXInput, parsedYInput, parsedZInput);

        JsonMapVisibility labelVisibility = new JsonMapVisibility(
                (float) labelMinVisibilitySlider.getVisibility(),
                (float) labelMaxVisibilitySlider.getVisibility(),
                (float) labelFadeSlider.getVisibility());
        JsonMapVisibility iconVisibility = new JsonMapVisibility(
                (float) iconMinVisibilitySlider.getVisibility(),
                (float) iconMaxVisibilitySlider.getVisibility(),
                (float) iconFadeSlider.getVisibility());

        JsonMapAttributes attributes = new JsonMapAttributesBuilder()
                .setLabel(label)
                .setIcon(iconId)
                .setPriority(priority)
                .setLabelColor(labelColorCache)
                .setLabelShadow(labelShadow)
                .setLabelVisibility(labelVisibility)
                .setIconColor(iconColorCache)
                .setIconVisibility(iconVisibility)
                .build();

        waypoint = new WaypointLocation(location, label, category, attributes);

        saveButton.active = !labelInput.getTextBoxInput().isBlank()
                && CustomColor.fromHexString(iconColorInput.getTextBoxInput()) != CustomColor.NONE
                && COORDINATE_PATTERN.matcher(xInput.getTextBoxInput()).matches()
                && (COORDINATE_PATTERN.matcher(yInput.getTextBoxInput()).matches()
                        || yInput.getTextBoxInput().isEmpty())
                && COORDINATE_PATTERN.matcher(zInput.getTextBoxInput()).matches()
                && priority > 0
                && priority <= 1000;
    }

    private void saveWaypoint() {
        if (oldWaypoint != null) {
            Services.Waypoints.removeWaypoint(oldWaypoint);
        }

        Services.Waypoints.addWaypoint(waypoint);
    }

    private void populateIcons() {
        iconButtons.clear();

        if (availableIcons.isEmpty()) return;

        int numIcons = availableIcons.size();
        int totalWidth = (int) (dividedWidth * 20) - (int) ((dividedWidth * 8) + 20);
        int buttonWidth = totalWidth / ICONS_PER_PAGE;
        int iconIndex;

        for (int i = 0; i < Math.min(availableIcons.size(), ICONS_PER_PAGE); i++) {
            iconIndex = (iconScrollOffset + i) % numIcons;
            MapIcon currentIcon = availableIcons.get(iconIndex);

            int xPos = (int) (dividedWidth * 8) + 20 + (i * buttonWidth);

            IconButton iconButton = new IconButton(
                    xPos, (int) (dividedHeight * 25), buttonWidth, currentIcon, iconIndex == selectedIconIndex);

            iconButtons.add(iconButton);
        }
    }

    private void handleLabelShadowClick(int button) {
        int index = labelShadow.ordinal();
        int numValues = TextShadow.values().length;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            index = (index + 1) % numValues;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            index = (index - 1 + numValues) % numValues;
        }

        labelShadow = TextShadow.values()[index];

        labelShadowButton.setMessage(Component.literal(labelShadow.name()));
    }

    private void handleIconTypeClick(int button) {
        int index = iconType.ordinal();
        int numValues = IconType.values().length;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            index = (index + 1) % numValues;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            index = (index - 1 + numValues) % numValues;
        }

        iconType = IconType.values()[index];

        iconTypeButton.setMessage(Component.literal(iconType.name()));

        previousIconButton.visible = iconType != IconType.NONE;
        nextIconButton.visible = iconType != IconType.NONE;
        iconColorInput.visible = iconType != IconType.NONE;
        addCustomIconButton.visible = iconType == IconType.CUSTOM;

        if (iconType == IconType.NONE && getFocusedTextInput() == iconColorInput) {
            this.setFocusedTextInput(null);
        }

        availableIcons.clear();
        iconScrollOffset = 0;

        if (iconType == IconType.WYNNTILS) {
            availableIcons.addAll(Services.MapData.MAP_ICONS_PROVIDER.getIcons().toList());
        } else if (iconType == IconType.CUSTOM) {
            availableIcons.addAll(Services.Waypoints.getCustomIcons());
        }

        nextIconButton.active = availableIcons.size() > ICONS_PER_PAGE;
        previousIconButton.active = availableIcons.size() > ICONS_PER_PAGE;
    }

    private void handleIconVisibilityClick(int button) {
        int index = iconVisibilityType.ordinal();
        int numValues = VisibilityType.values().length;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            index = (index + 1) % numValues;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            index = (index - 1 + numValues) % numValues;
        }

        iconVisibilityType = VisibilityType.values()[index];

        iconVisiblityButton.setMessage(Component.literal(iconVisibilityType.name()));

        if (iconVisibilityType == VisibilityType.ALWAYS) {
            iconMinVisibilitySlider.setVisibility(
                    FixedMapVisibility.ICON_ALWAYS.getMin().get());
            iconMaxVisibilitySlider.setVisibility(
                    FixedMapVisibility.ICON_ALWAYS.getMax().get());
            iconFadeSlider.setVisibility(
                    FixedMapVisibility.ICON_ALWAYS.getFade().get());
        } else if (iconVisibilityType == VisibilityType.NEVER) {
            iconMinVisibilitySlider.setVisibility(
                    FixedMapVisibility.ICON_NEVER.getMin().get());
            iconMaxVisibilitySlider.setVisibility(
                    FixedMapVisibility.ICON_NEVER.getMax().get());
            iconFadeSlider.setVisibility(FixedMapVisibility.ICON_NEVER.getFade().get());
        }
    }

    private void handleLabelVisibilityClick(int button) {
        int index = labelVisibilityType.ordinal();
        int numValues = VisibilityType.values().length;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            index = (index + 1) % numValues;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            index = (index - 1 + numValues) % numValues;
        }

        labelVisibilityType = VisibilityType.values()[index];

        labelVisiblityButton.setMessage(Component.literal(labelVisibilityType.name()));

        if (labelVisibilityType == VisibilityType.ALWAYS) {
            labelMinVisibilitySlider.setVisibility(
                    FixedMapVisibility.ICON_ALWAYS.getMin().get());
            labelMaxVisibilitySlider.setVisibility(
                    FixedMapVisibility.ICON_ALWAYS.getMax().get());
            labelFadeSlider.setVisibility(
                    FixedMapVisibility.ICON_ALWAYS.getFade().get());
        } else if (labelVisibilityType == VisibilityType.NEVER) {
            labelMinVisibilitySlider.setVisibility(
                    FixedMapVisibility.ICON_NEVER.getMin().get());
            labelMaxVisibilitySlider.setVisibility(
                    FixedMapVisibility.ICON_NEVER.getMax().get());
            labelFadeSlider.setVisibility(
                    FixedMapVisibility.ICON_NEVER.getFade().get());
        }
    }

    private void toggleWidgets() {
        labelInput.visible = !visibilityTab;
        labelShadowButton.visible = !visibilityTab;
        labelColorInput.visible = !visibilityTab;
        iconTypeButton.visible = !visibilityTab;
        previousIconButton.visible = !visibilityTab && iconType != IconType.NONE;
        nextIconButton.visible = !visibilityTab && iconType != IconType.NONE;
        iconColorInput.visible = !visibilityTab;
        xInput.visible = !visibilityTab;
        yInput.visible = !visibilityTab;
        zInput.visible = !visibilityTab;
        centerOnPlayerButton.visible = !visibilityTab;
        centerOnWorldButton.visible = !visibilityTab;
        priorityInput.visible = visibilityTab;
        labelVisiblityButton.visible = visibilityTab;
        iconVisiblityButton.visible = visibilityTab;
        labelMinVisibilitySlider.visible = visibilityTab;
        labelMaxVisibilitySlider.visible = visibilityTab;
        labelFadeSlider.visible = visibilityTab;
        iconMinVisibilitySlider.visible = visibilityTab;
        iconMaxVisibilitySlider.visible = visibilityTab;
        iconFadeSlider.visible = visibilityTab;
    }

    private static final class OptionButton extends WynntilsButton {
        private static final int BUTTON_HEIGHT = 20;

        private OptionButton(int x, int y, int width, Component message) {
            super(x, y, width, BUTTON_HEIGHT, message);
        }

        @Override
        public void onPress() {
            // Handle in mouseClicked to use left/right click
        }
    }

    private final class VisibilitySlider extends AbstractSliderButton {
        private static final int BUTTON_HEIGHT = 20;

        private VisibilitySlider(int x, int y, int width, Component message, double value) {
            super(x, y, width, BUTTON_HEIGHT, message, value);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(String.valueOf(getVisibility())));
        }

        @Override
        protected void applyValue() {
            updateWaypoint();
        }

        public int getVisibility() {
            return (int) (value * 100);
        }

        public void setVisibility(Float visibility) {
            this.value = (double) visibility / 100;

            updateMessage();
        }
    }

    private enum IconType {
        NONE,
        WYNNTILS,
        CUSTOM
    }

    private enum VisibilityType {
        ALWAYS,
        NEVER,
        CUSTOM
    }
}
