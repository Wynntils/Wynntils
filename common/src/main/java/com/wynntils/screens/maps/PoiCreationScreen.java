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
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.screens.base.widgets.ColorPickerWidget;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.widgets.IconButton;
import com.wynntils.services.map.pois.CustomPoi;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.mc.type.PoiLocation;
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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class PoiCreationScreen extends AbstractMapScreen {
    // Constants
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("[-+]?\\d{1,8}");
    private static final float GRID_DIVISIONS = 64.0f;
    private static final int ICONS_PER_PAGE = 5;

    // Collections
    private final List<IconButton> iconButtons = new ArrayList<>();

    // Widgets
    private Button saveButton;
    private TextInputBoxWidget nameInput;
    private TextInputBoxWidget xInput;
    private TextInputBoxWidget yInput;
    private TextInputBoxWidget zInput;
    private TextInputBoxWidget colorInput;
    private TextInputBoxWidget focusedTextInput;

    // UI Size, positions etc
    private float dividedWidth;
    private float dividedHeight;

    // Screen information
    private final Screen returnScreen;
    private boolean firstSetup;
    private int iconScrollOffset = 0;
    private Location setupLocation;
    private CustomPoi oldPoi;

    // Poi details
    private CustomColor colorCache = CommonColors.WHITE;
    private Integer parsedXInput;
    private Integer parsedYInput;
    private Integer parsedZInput;
    private Texture selectedIcon;
    private CustomPoi.Visibility selectedVisibility = CustomPoi.Visibility.DEFAULT;

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

    private PoiCreationScreen(MainMapScreen oldMapScreen, CustomPoi poi) {
        this(oldMapScreen);

        this.oldPoi = poi;
        this.firstSetup = true;
    }

    private PoiCreationScreen(PoiManagementScreen managementScreen, CustomPoi poi) {
        super();
        this.returnScreen = managementScreen;

        this.oldPoi = poi;
        this.firstSetup = true;
    }

    public static Screen create(MainMapScreen oldMapScreen) {
        return new PoiCreationScreen(oldMapScreen);
    }

    public static Screen create(MainMapScreen oldMapScreen, Location setupLocation) {
        return new PoiCreationScreen(oldMapScreen, setupLocation);
    }

    public static Screen create(MainMapScreen oldMapScreen, CustomPoi poi) {
        return new PoiCreationScreen(oldMapScreen, poi);
    }

    public static Screen create(PoiManagementScreen managementScreen, CustomPoi poi) {
        return new PoiCreationScreen(managementScreen, poi);
    }

    @Override
    protected void doInit() {
        dividedWidth = this.width / GRID_DIVISIONS;
        dividedHeight = this.height / GRID_DIVISIONS;

        if (firstSetup) {
            updateMapCenter(McUtils.player().getBlockX(), McUtils.player().getBlockZ());
        }

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

        // region Name
        nameInput = new TextInputBoxWidget(
                (int) (dividedWidth * 4),
                (int) (dividedHeight * 14),
                (int) (dividedWidth * 12),
                20,
                (s) -> updateSaveStatus(),
                this,
                nameInput);
        this.addRenderableWidget(nameInput);

        if (oldPoi != null && firstSetup) {
            nameInput.setTextBoxInput(oldPoi.getName());
        }

        if (firstSetup) {
            setFocusedTextInput(nameInput);
        }
        // endregion

        // region Color
        colorInput = new TextInputBoxWidget(
                (int) (dividedWidth * 19),
                (int) (dividedHeight * 14),
                (int) (dividedWidth * 6),
                20,
                (s) -> {
                    CustomColor color = CustomColor.fromHexString(s);

                    if (color == CustomColor.NONE) {
                        // Default to white
                        colorCache = CommonColors.WHITE;
                        colorInput.setRenderColor(CommonColors.RED);
                    } else {
                        colorCache = color;
                        colorInput.setRenderColor(CommonColors.GREEN);
                    }

                    updateSaveStatus();
                },
                this,
                colorInput);
        this.addRenderableWidget(colorInput);

        if (oldPoi != null && firstSetup) {
            colorInput.setTextBoxInput(String.valueOf(oldPoi.getColor().toHexString()));
        } else if (colorInput.getTextBoxInput().isEmpty()) {
            colorInput.setTextBoxInput("#FFFFFF");
        }

        this.addRenderableWidget(
                new ColorPickerWidget((int) (dividedWidth * 26), (int) (dividedHeight * 14), 20, 20, colorInput));
        // endregion

        // region Icon
        this.addRenderableWidget(new Button.Builder(Component.literal("<"), (button) -> {
                    if (iconScrollOffset - 1 < 0) {
                        iconScrollOffset = Services.Poi.POI_ICONS.size() - 1;
                    } else {
                        iconScrollOffset--;
                    }

                    populateIcons();
                })
                .pos((int) (dividedWidth * 8), (int) (dividedHeight * 25))
                .size(20, 20)
                .build());

        this.addRenderableWidget(new Button.Builder(Component.literal(">"), (button) -> {
                    if (iconScrollOffset + 1 >= Services.Poi.POI_ICONS.size()) {
                        iconScrollOffset = 0;
                    } else {
                        iconScrollOffset++;
                    }

                    populateIcons();
                })
                .pos((int) (dividedWidth * 20), (int) (dividedHeight * 25))
                .size(20, 20)
                .build());

        if (firstSetup) {
            if (oldPoi != null) {
                Optional<Texture> oldIcon = Services.Poi.POI_ICONS.stream()
                        .filter(icon -> icon == oldPoi.getIcon())
                        .findFirst();

                oldIcon.ifPresent(icon -> selectedIcon = icon);

                iconScrollOffset = Services.Poi.POI_ICONS.indexOf(selectedIcon);
            } else {
                selectedIcon = Services.Poi.POI_ICONS.getFirst();
            }
        }
        // endregion

        // region Coordinates
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
                    updateSaveStatus();
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
                    updateSaveStatus();
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
                    updateSaveStatus();
                },
                this,
                zInput);
        this.addRenderableWidget(zInput);

        if (firstSetup) {
            if (oldPoi != null) {
                xInput.setTextBoxInput(String.valueOf(oldPoi.getLocation().getX()));
                int yValue = oldPoi.getLocation().getY().isPresent()
                        ? oldPoi.getLocation().getY().get()
                        : 0;
                yInput.setTextBoxInput(String.valueOf(yValue));
                zInput.setTextBoxInput(String.valueOf(oldPoi.getLocation().getZ()));
            } else if (setupLocation != null) {
                xInput.setTextBoxInput(String.valueOf(setupLocation.x()));
                yInput.setTextBoxInput(String.valueOf(setupLocation.y()));
                zInput.setTextBoxInput(String.valueOf(setupLocation.z()));
            }
        }

        this.addRenderableWidget(new Button.Builder(Component.literal("🧍"), (button) -> {
                    xInput.setTextBoxInput(String.valueOf(McUtils.player().getBlockX()));
                    yInput.setTextBoxInput(String.valueOf(McUtils.player().getBlockY()));
                    zInput.setTextBoxInput(String.valueOf(McUtils.player().getBlockZ()));
                })
                .pos((int) (dividedWidth * 26), (int) (dividedHeight * 36))
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiCreation.centerPlayer")))
                .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("🌍"), (button) -> {
                    xInput.setTextBoxInput(String.valueOf(MAP_CENTER_X));
                    yInput.setTextBoxInput("0");
                    zInput.setTextBoxInput(String.valueOf(MAP_CENTER_Z));
                })
                .pos((int) (dividedWidth * 29), (int) (dividedHeight * 36))
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiCreation.centerWorld")))
                .build());
        // endregion

        // region Visibility
        this.addRenderableWidget(new Button.Builder(
                        Component.literal("<"),
                        (button) -> selectedVisibility = CustomPoi.Visibility.values()[
                                (selectedVisibility.ordinal() - 1 + CustomPoi.Visibility.values().length)
                                        % CustomPoi.Visibility.values().length])
                .pos((int) (dividedWidth * 8), (int) (dividedHeight * 47))
                .size(20, 20)
                .build());
        this.addRenderableWidget(new Button.Builder(
                        Component.literal(">"),
                        (button) -> selectedVisibility = CustomPoi.Visibility.values()[
                                (selectedVisibility.ordinal() + 1 + CustomPoi.Visibility.values().length)
                                        % CustomPoi.Visibility.values().length])
                .pos((int) (dividedWidth * 22) - 19, (int) (dividedHeight * 47))
                .size(20, 20)
                .build());

        if (oldPoi != null && firstSetup) {
            selectedVisibility = oldPoi.getVisibility();
        }
        // endregion

        // region Screen Interactions
        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.poiCreation.cancel"), (button) -> this.onClose())
                .pos((int) (dividedWidth * 6), (int) (dividedHeight * 54))
                .size((int) (dividedWidth * 8), 20)
                .build());

        saveButton = new Button.Builder(Component.translatable("screens.wynntils.poiCreation.save"), (button) -> {
                    savePoi();
                    this.onClose();
                })
                .pos((int) (dividedWidth * 18), (int) (dividedHeight * 54))
                .size((int) (dividedWidth * 8), 20)
                .build();
        this.addRenderableWidget(saveButton);
        // endregion

        updateSaveStatus();
        populateIcons();
        firstSetup = false;
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderBlurredBackground();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();
        renderMap(guiGraphics);
        RenderUtils.enableScissor(
                guiGraphics,
                (int) (renderX + renderedBorderXOffset),
                (int) (renderY + renderedBorderYOffset),
                (int) mapWidth,
                (int) mapHeight);

        if (parsedXInput != null && parsedZInput != null) {
            Poi poi = new CustomPoi(
                    new PoiLocation(parsedXInput, null, parsedZInput),
                    nameInput.getTextBoxInput(),
                    CustomColor.fromHexString(colorInput.getTextBoxInput()) == CustomColor.NONE
                            ? CommonColors.WHITE
                            : CustomColor.fromHexString(colorInput.getTextBoxInput()),
                    selectedIcon,
                    selectedVisibility);

            MultiBufferSource.BufferSource bufferSource =
                    McUtils.mc().renderBuffers().bufferSource();

            poi.renderAt(
                    poseStack,
                    bufferSource,
                    MapRenderer.getRenderX(poi, mapCenterX, centerX, zoomRenderScale),
                    MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, zoomRenderScale),
                    hovered == poi,
                    1,
                    zoomRenderScale,
                    zoomLevel,
                    true);

            bufferSource.endBatch();
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

        RenderUtils.disableScissor(guiGraphics);

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.poiCreation.waypointName") + ":"),
                        (int) (dividedWidth * 4),
                        (int) (dividedHeight * 12.5f),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

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

        renderIcons(guiGraphics, mouseX, mouseY, partialTick);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.poiCreation.color") + ":"),
                        dividedWidth * 19f,
                        (int) (dividedHeight * 12.5f),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.poiCreation.visibility") + ":"),
                        dividedWidth * 10.0f,
                        dividedHeight * 45.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(I18n.get(selectedVisibility.getTranslationKey())),
                        dividedWidth * 15.0f,
                        dividedWidth * 15.0f,
                        dividedHeight * 47.0f,
                        dividedHeight * 47.0f + 20,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    public void setSelectedIcon(Texture selectedIcon) {
        this.selectedIcon = selectedIcon;

        populateIcons();
        updateSaveStatus();
    }

    private void renderIcons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (IconButton iconButton : iconButtons) {
            iconButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            int gameX = (int) ((mouseX - centerX) / zoomRenderScale + mapCenterX);
            int gameZ = (int) ((mouseY - centerZ) / zoomRenderScale + mapCenterZ);
            xInput.setTextBoxInput(String.valueOf(gameX));
            zInput.setTextBoxInput(String.valueOf(gameZ));
        }

        for (IconButton iconButton : iconButtons) {
            if (iconButton.isMouseOver(mouseX, mouseY)) {
                return iconButton.mouseClicked(mouseX, mouseY, button);
            }
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
    public void onClose() {
        McUtils.setScreen(returnScreen);
    }

    private void updateSaveStatus() {
        if (saveButton == null) return;

        saveButton.active = !nameInput.getTextBoxInput().isBlank()
                && CustomColor.fromHexString(colorInput.getTextBoxInput()) != CustomColor.NONE
                && COORDINATE_PATTERN.matcher(xInput.getTextBoxInput()).matches()
                && (COORDINATE_PATTERN.matcher(yInput.getTextBoxInput()).matches()
                        || yInput.getTextBoxInput().isEmpty())
                && COORDINATE_PATTERN.matcher(zInput.getTextBoxInput()).matches();
    }

    private void savePoi() {
        CustomPoi poi = new CustomPoi(
                new PoiLocation(
                        Integer.parseInt(xInput.getTextBoxInput()),
                        yInput.getTextBoxInput().isEmpty() ? 0 : Integer.parseInt(yInput.getTextBoxInput()),
                        Integer.parseInt(zInput.getTextBoxInput())),
                nameInput.getTextBoxInput(),
                CustomColor.fromHexString(colorInput.getTextBoxInput()),
                selectedIcon,
                selectedVisibility);

        HiddenConfig<List<CustomPoi>> customPoiConfig =
                Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;
        List<CustomPoi> customPois = customPoiConfig.get();
        if (oldPoi != null) {
            customPois.set(customPois.indexOf(oldPoi), poi);
        } else {
            customPois.add(poi);
        }

        customPoiConfig.touched();
        Managers.Feature.getFeatureInstance(MainMapFeature.class).updateWaypoints();
    }

    private void populateIcons() {
        iconButtons.clear();

        int numIcons = Services.Poi.POI_ICONS.size();
        int totalWidth = (int) (dividedWidth * 20) - (int) ((dividedWidth * 8) + 20);
        int buttonWidth = totalWidth / ICONS_PER_PAGE;
        int iconIndex;

        for (int i = 0; i < Math.min(Services.Poi.POI_ICONS.size(), ICONS_PER_PAGE); i++) {
            iconIndex = (iconScrollOffset + i) % numIcons;
            Texture currentIcon = Services.Poi.POI_ICONS.get(iconIndex);

            int xPos = (int) (dividedWidth * 8) + 20 + (i * buttonWidth);

            IconButton iconButton = new IconButton(
                    xPos, (int) (dividedHeight * 25), buttonWidth, currentIcon, currentIcon == selectedIcon);

            iconButtons.add(iconButton);
        }
    }
}
