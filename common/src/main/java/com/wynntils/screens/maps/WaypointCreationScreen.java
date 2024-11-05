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
import com.wynntils.screens.base.widgets.ColorPickerWidget;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.maps.widgets.IconButton;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.impl.MapLocationAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapVisibilityImpl;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.builtin.WaypointLocation;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class WaypointCreationScreen extends AbstractMapScreen {
    // Constants
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("[-+]?\\d{1,8}");
    private static final float GRID_DIVISIONS = 64.0f;
    private static final int ICONS_PER_PAGE = 5;

    // Collections
    private final List<MapIcon> availableIcons = new ArrayList<>();
    private final List<IconButton> iconButtons = new ArrayList<>();

    // Widgets
    private Button addCustomIconButton;
    private Button previousIconButton;
    private Button centerOnPlayerButton;
    private Button centerOnWorldButton;
    private Button nextIconButton;
    private Button saveButton;
    private Button editIconVisibilityButton;
    private Button editLabelVisibilityButton;
    private ColorPickerWidget iconColorPicker;
    private ColorPickerWidget labelColorPicker;
    private OptionButton labelShadowButton;
    private TextInputBoxWidget labelInput;
    private TextInputBoxWidget labelColorInput;
    private TextInputBoxWidget iconColorInput;
    private TextInputBoxWidget xInput;
    private TextInputBoxWidget yInput;
    private TextInputBoxWidget zInput;
    private TextInputBoxWidget focusedTextInput;
    private WynntilsCheckbox iconCheckbox;

    // UI Size, positions etc
    private float dividedWidth;
    private float dividedHeight;

    // Screen information
    private final Screen returnScreen;
    private boolean firstSetup;
    private int iconScrollOffset = 0;
    private Location setupLocation;
    private WaypointLocation oldWaypoint;

    // Waypoint details
    private boolean useIcon = true;
    private CustomColor iconColorCache = CommonColors.WHITE;
    private CustomColor labelColorCache = CommonColors.WHITE;
    private Integer parsedXInput;
    private Integer parsedYInput;
    private Integer parsedZInput;
    private MapVisibilityImpl iconVisibility;
    private MapVisibilityImpl labelVisibility;
    private MapIcon selectedIcon;
    private String category = "";
    private String iconId = MapIcon.NO_ICON_ID;
    private String label = "";
    private TextShadow labelShadow = TextShadow.NORMAL;
    private WaypointLocation waypoint;

    private WaypointCreationScreen(MainMapScreen oldMapScreen) {
        super();
        this.returnScreen = oldMapScreen;

        this.firstSetup = true;
    }

    private WaypointCreationScreen(MainMapScreen oldMapScreen, Location setupLocation) {
        this(oldMapScreen);

        this.setupLocation = setupLocation;
        this.firstSetup = true;
    }

    private WaypointCreationScreen(MainMapScreen oldMapScreen, WaypointLocation oldWaypoint) {
        this(oldMapScreen);

        this.oldWaypoint = oldWaypoint;
        this.firstSetup = true;
    }

    private WaypointCreationScreen(PoiManagementScreen managementScreen, WaypointLocation oldWaypoint) {
        super();
        this.returnScreen = managementScreen;

        this.oldWaypoint = oldWaypoint;
        this.firstSetup = true;
    }

    public static Screen create(MainMapScreen oldMapScreen) {
        return new WaypointCreationScreen(oldMapScreen);
    }

    public static Screen create(MainMapScreen oldMapScreen, Location setupLocation) {
        return new WaypointCreationScreen(oldMapScreen, setupLocation);
    }

    public static Screen create(MainMapScreen oldMapScreen, WaypointLocation oldWaypoint) {
        return new WaypointCreationScreen(oldMapScreen, oldWaypoint);
    }

    public static Screen create(PoiManagementScreen managementScreen, WaypointLocation oldWaypoint) {
        return new WaypointCreationScreen(managementScreen, oldWaypoint);
    }

    @Override
    protected void doInit() {
        availableIcons.clear();

        if (firstSetup) {
            updateMapCenter(McUtils.player().getBlockX(), McUtils.player().getBlockZ());
        }

        ResolvedMapAttributes oldAttributes = null;

        if (oldWaypoint != null) {
            oldAttributes = Services.MapData.resolveMapAttributes(oldWaypoint);
        }

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

        if (firstSetup) {
            if (oldAttributes != null) {
                labelInput.setTextBoxInput(oldAttributes.label());
                labelShadow = oldAttributes.labelShadow();
            }

            setFocusedTextInput(labelInput);
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

        if (firstSetup && oldAttributes != null) {
            labelColorInput.setTextBoxInput(oldAttributes.labelColor().toHexString());
        } else if (labelColorInput.getTextBoxInput().isEmpty()) {
            labelColorInput.setTextBoxInput("#FFFFFF");
        }

        labelColorPicker =
                new ColorPickerWidget((int) (dividedWidth * 29), (int) (dividedHeight * 14), 20, 20, labelColorInput);
        this.addRenderableWidget(labelColorPicker);
        // endregion

        // region Icon
        availableIcons.clear();
        availableIcons.addAll(Services.MapData.getIcons().toList());

        if (firstSetup && oldAttributes != null) {
            String oldIconId = oldAttributes.iconId();

            if (oldIconId.equals(MapIcon.NO_ICON_ID)) {
                useIcon = false;
            } else {
                Optional<MapIcon> oldMapIcon = availableIcons.stream()
                        .filter(mapIcon -> mapIcon.getIconId().equals(oldIconId))
                        .findFirst();

                oldMapIcon.ifPresent(oldIcon -> selectedIcon = oldIcon);

                iconScrollOffset = availableIcons.indexOf(selectedIcon);

                iconId = oldIconId;
            }
        }

        iconCheckbox = new WynntilsCheckbox(
                (int) dividedWidth,
                (int) (dividedHeight * 25),
                20,
                Component.translatable("screens.wynntils.waypointCreation.icon"),
                useIcon,
                (int) (dividedWidth * 6),
                ((checkbox, bl) -> toggleIcon()));
        this.addRenderableWidget(iconCheckbox);

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

        previousIconButton.visible = useIcon;

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

        nextIconButton.visible = useIcon;

        // Keep this buttons width the same as the area for the displayed icons
        int iconButtonWidth = (int) (dividedWidth * 20) - (int) ((dividedWidth * 8) + 20);

        addCustomIconButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointCreation.addCustomIcon"),
                        (button) -> McUtils.mc().setScreen(CustomWaypointIconScreen.create(this)))
                .pos((int) (dividedWidth * 8) + 20, (int) (dividedHeight * 25) + 20)
                .size(iconButtonWidth, 20)
                .build();
        this.addRenderableWidget(addCustomIconButton);

        addCustomIconButton.visible = useIcon;

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

        iconColorInput.visible = useIcon;

        if (firstSetup && oldAttributes != null) {
            iconColorInput.setTextBoxInput(oldAttributes.iconColor().toHexString());
        } else if (iconColorInput.getTextBoxInput().isEmpty()) {
            iconColorInput.setTextBoxInput("#FFFFFF");
        }

        iconColorPicker =
                new ColorPickerWidget((int) (dividedWidth * 29), (int) (dividedHeight * 25), 20, 20, iconColorInput);
        this.addRenderableWidget(iconColorPicker);

        iconColorPicker.visible = useIcon;
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
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.waypointCreation.centerPlayer")))
                .build();
        this.addRenderableWidget(centerOnPlayerButton);

        centerOnWorldButton = new Button.Builder(Component.literal("🌍"), (button) -> {
                    xInput.setTextBoxInput(String.valueOf(MAP_CENTER_X));
                    yInput.setTextBoxInput("0");
                    zInput.setTextBoxInput(String.valueOf(MAP_CENTER_Z));
                })
                .pos((int) (dividedWidth * 29), (int) (dividedHeight * 36))
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.waypointCreation.centerWorld")))
                .build();
        this.addRenderableWidget(centerOnWorldButton);
        // endregion

        // region Category
        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.waypointCreation.changeCategory"),
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
        editLabelVisibilityButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointCreation.editLabelVisibility"),
                        (button) -> McUtils.mc().setScreen(WaypointVisibilityScreen.create(this, waypoint, true)))
                .pos((int) (dividedWidth * 3), (int) (dividedHeight * 42))
                .size((int) (dividedWidth * 12), 20)
                .build();
        this.addRenderableWidget(editLabelVisibilityButton);

        editIconVisibilityButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointCreation.editIconVisibility"),
                        (button) -> McUtils.mc().setScreen(WaypointVisibilityScreen.create(this, waypoint, false)))
                .pos((int) (dividedWidth * 17), (int) (dividedHeight * 42))
                .size((int) (dividedWidth * 12), 20)
                .build();
        this.addRenderableWidget(editIconVisibilityButton);

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.waypointCreation.cancel"), (button) -> this.onClose())
                .pos((int) (dividedWidth * 4), (int) (dividedHeight * 56))
                .size((int) (dividedWidth * 9), 20)
                .build());

        saveButton = new Button.Builder(Component.translatable("screens.wynntils.waypointCreation.save"), (button) -> {
                    saveWaypoint();
                    this.onClose();
                })
                .pos((int) (dividedWidth * 17), (int) (dividedHeight * 56))
                .size((int) (dividedWidth * 9), 20)
                .build();
        this.addRenderableWidget(saveButton);
        // endregion

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
            renderMapFeatures(poseStack, mouseX, mouseY);
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

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.waypointCreation.title")),
                        dividedWidth * 15,
                        dividedHeight * 6,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        2f);

        // region Label
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.waypointCreation.label") + ":"),
                        dividedWidth,
                        dividedHeight * 12.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.waypointCreation.labelShadow") + ":"),
                        dividedWidth * 12,
                        dividedHeight * 12.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.waypointCreation.labelColor") + ":"),
                        dividedWidth * 23,
                        dividedHeight * 12.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
        // endregion

        // region Icon
        if (useIcon) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.waypointCreation.iconColor") + ":"),
                            dividedWidth * 23.0f,
                            dividedHeight * 23.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

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

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.waypointCreation.currentCategory") + ": "),
                        dividedWidth * 2.0f,
                        dividedHeight * 48.0f + 10,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderScrollingText(
                        poseStack,
                        StyledText.fromString(category.isEmpty() ? "DEFAULT" : category),
                        dividedWidth * 2.0f,
                        dividedHeight * 51.0f + 10,
                        dividedWidth * 29.0f,
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
            yInput.setTextBoxInput("0");
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

    @Override
    protected Stream<MapFeature> getRenderedMapFeatures() {
        return Stream.of(waypoint);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSelectedIcon(MapIcon selectedIcon) {
        this.selectedIcon = selectedIcon;

        populateIcons();
        updateIcon();
        updateWaypoint();
    }

    public WaypointLocation getWaypoint() {
        return waypoint;
    }

    public void updateVisibility(MapVisibilityImpl visibility, boolean label) {
        if (label) {
            labelVisibility = visibility;
        } else {
            iconVisibility = visibility;
        }

        updateWaypoint();
    }

    private void renderIcons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (IconButton iconButton : iconButtons) {
            iconButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void updateIcon() {
        if (selectedIcon == null) return;

        iconId = selectedIcon.getIconId();
    }

    private void updateWaypoint() {
        if (saveButton == null) return;

        if (parsedXInput == null || parsedYInput == null || parsedZInput == null) {
            saveButton.active = false;
            editLabelVisibilityButton.active = false;
            editIconVisibilityButton.active = false;
            return;
        }

        Location location = new Location(parsedXInput, parsedYInput, parsedZInput);

        Optional<MapCategory> waypointCategoryOpt = Services.MapData.getCategoryDefinitions(
                        "wynntils:personal:waypoint")
                .findFirst();

        int defaultPriority = waypointCategoryOpt
                .flatMap(waypointCategory -> waypointCategory.getAttributes().flatMap(MapAttributes::getPriority))
                .orElse(1000);

        MapLocationAttributesImpl attributes = new MapAttributesBuilder()
                .setLabel(label)
                .setIcon(iconId)
                .setPriority(defaultPriority)
                .setLabelColor(labelColorCache)
                .setLabelShadow(labelShadow)
                .setLabelVisibility(labelVisibility)
                .setIconColor(iconColorCache)
                .setIconVisibility(iconVisibility)
                .asLocationAttributes()
                .build();

        waypoint = new WaypointLocation(location, label, category, attributes);

        boolean validWaypoint = !labelInput.getTextBoxInput().isBlank()
                && CustomColor.fromHexString(iconColorInput.getTextBoxInput()) != CustomColor.NONE
                && COORDINATE_PATTERN.matcher(xInput.getTextBoxInput()).matches()
                && (COORDINATE_PATTERN.matcher(yInput.getTextBoxInput()).matches()
                        || yInput.getTextBoxInput().isEmpty())
                && COORDINATE_PATTERN.matcher(zInput.getTextBoxInput()).matches();

        saveButton.active = validWaypoint;
        editLabelVisibilityButton.active = validWaypoint;
        editIconVisibilityButton.active = validWaypoint && useIcon;
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
                    xPos, (int) (dividedHeight * 25), buttonWidth, currentIcon, currentIcon == selectedIcon);

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

    private void toggleIcon() {
        useIcon = !useIcon;

        previousIconButton.visible = useIcon;
        nextIconButton.visible = useIcon;
        addCustomIconButton.visible = useIcon;
        iconColorInput.visible = useIcon;
        iconColorPicker.visible = useIcon;

        if (!useIcon && getFocusedTextInput() == iconColorInput) {
            this.setFocusedTextInput(null);
        }

        if (useIcon) {
            availableIcons.addAll(Services.MapData.getIcons().toList());

            if (selectedIcon != null) {
                iconId = selectedIcon.getIconId();
            }
        } else {
            if (getFocusedTextInput() == iconColorInput) {
                this.setFocusedTextInput(null);
            }

            iconId = MapIcon.NO_ICON_ID;
            availableIcons.clear();
        }

        updateWaypoint();

        nextIconButton.active = availableIcons.size() > ICONS_PER_PAGE;
        previousIconButton.active = availableIcons.size() > ICONS_PER_PAGE;
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
}
