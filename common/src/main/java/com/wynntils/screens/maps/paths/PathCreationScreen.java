/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.FeatureCategoryScreen;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.StyledInfoButton;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.AbstractOptionWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.ColorOptionWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.FloatSliderOptionWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.TextShadowOptionWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.VisiblityOptionWidget;
import com.wynntils.screens.maps.paths.widgets.PathNotesOptionWidget;
import com.wynntils.screens.maps.paths.widgets.PathOptionsScrollBarWidget;
import com.wynntils.screens.maps.paths.widgets.PathTextureOptionWidget;
import com.wynntils.screens.maps.paths.widgets.PathToggleOptionWidget;
import com.wynntils.services.mapdata.MapPathsService;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.impl.MapPathAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapVisibilityImpl;
import com.wynntils.services.mapdata.attributes.resolving.MapAttributesResolver;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.impl.MapPathImpl;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class PathCreationScreen extends WynntilsScreen {
    private static final int INFO_BUTTON_X = 541;

    private final Screen previous;
    private final boolean editing;
    private final MapAttributesBuilder settings;
    private final List<OptionBinding<?>> bindings = new ArrayList<>();
    private PathOptionsScrollBarWidget worldOptions;
    private PathOptionsScrollBarWidget mapOptions;
    private PathToggleOptionWidget recordChests;
    private StyledButton saveButton;
    private StyledInfoButton infoButton;
    private String category;
    private String label;
    private boolean recordHighlights;
    private String error = "";
    private int x;
    private int y;

    private PathCreationScreen(Screen previous) {
        super(Component.translatable(
                Services.MapPaths.getEditing() == null
                        ? "screens.wynntils.map.paths.title"
                        : "screens.wynntils.map.paths.editTitle"));
        this.previous = previous;
        editing = Services.MapPaths.getEditing() != null;
        settings = new MapAttributesBuilder().from(Services.MapPaths.getAttributes());
        category = Services.MapPaths.getCategory();
        label = Services.MapPaths.getLabel();
        recordHighlights = Services.MapPaths.isRecordingHighlights();
    }

    public static Screen create(Screen previous) {
        if (!Services.MapPaths.hasDraft()) {
            Services.MapPaths.cancel();
        }

        return new PathCreationScreen(previous);
    }

    @Override
    protected void doInit() {
        super.doInit();
        int worldScroll = worldOptions == null ? 0 : worldOptions.scrollOffsetY;
        int mapScroll = mapOptions == null ? 0 : mapOptions.scrollOffsetY;
        bindings.clear();
        recordChests = null;
        setFocusedTextInput(null);
        x = (width - Texture.MANAGER_BACKGROUND.width()) / 2;
        y = (height - Texture.MANAGER_BACKGROUND.height()) / 2;
        int left = x + 15;
        int right = x + 290;

        infoButton = addRenderableWidget(new StyledInfoButton(
                x + INFO_BUTTON_X,
                y + 18,
                text("info"),
                List.of(
                        text("info").copy().withStyle(ChatFormatting.GOLD),
                        text("save.tooltip").copy().withStyle(ChatFormatting.GRAY),
                        Component.empty(),
                        text("info.hide", Component.translatable("screens.wynntils.map.providerManager.name"))
                                .copy()
                                .withStyle(ChatFormatting.GRAY),
                        Component.empty(),
                        text("info.remove", "wynntils/localProviders").copy().withStyle(ChatFormatting.GRAY))));

        TexturedTextInputBoxWidget name = new TexturedTextInputBoxWidget(
                left, y + 49, 270, 20, value -> label = value, this, TexturedTextInputBoxWidget.Mode.STRING);
        name.setTextBoxInput(label);
        addRenderableWidget(name);
        StyledButton categoryButton = button(right, y + 49, 271, "category", Texture.MANAGER_WIDGET_BACKGROUND, () -> {
            configure();
            McUtils.setScreen(new FeatureCategoryScreen(
                    this,
                    "",
                    category,
                    Services.MapData.allPossibleCategories(),
                    () -> Services.MapData.getProviderInfos().values().stream()
                            .flatMap(info -> info.provider().getFeatures())
                            .filter(MapPath.class::isInstance)
                            .map(MapPath.class::cast),
                    null,
                    selected -> category = selected));
        });
        categoryButton.setMessage(Component.literal(category));

        worldOptions = addRenderableWidget(new PathOptionsScrollBarWidget(left, y + 97, 270, 177, text("worldHeader")));
        mapOptions = addRenderableWidget(new PathOptionsScrollBarWidget(right, y + 97, 271, 177, text("mapHeader")));
        worldOptions.scrollOffsetY = worldScroll;
        mapOptions.scrollOffsetY = mapScroll;
        MapPathAttributesImpl own = settings.asPathAttributes().build();
        MapPathAttributesImpl inherited = inheritedStyle();
        bind(
                worldOptions,
                new ColorOptionWidget(
                        text("worldColor"),
                        text("pathColor.description"),
                        OptionCategory.PATH,
                        MapAttributes::getPathColor,
                        this,
                        true),
                MapAttributesBuilder::setPathColor,
                own,
                inherited);
        bind(worldOptions, new PathTextureOptionWidget(), MapAttributesBuilder::setPathIcon, own, inherited);
        bind(
                worldOptions,
                new PathToggleOptionWidget(text("hasPath"), text("hasPath.description"), MapAttributes::getHasPath),
                MapAttributesBuilder::setHasPath,
                own,
                inherited);
        if (!editing) {
            recordChests = new PathToggleOptionWidget(
                    text("recordHighlights"),
                    text("recordHighlights.description"),
                    attributes -> Optional.of(recordHighlights));
            recordChests.updateFromAttributes(Optional.of(own), Optional.of(inherited));
            worldOptions.addWidget(recordChests);
        }

        worldOptions.addWidget(
                new PathNotesOptionWidget(Services.MapPaths::getNoteCount, Services.MapPaths::canEditNotes, () -> {
                    configure();
                    McUtils.setScreen(new PathNoteScreen(this));
                }));

        bind(
                mapOptions,
                new ColorOptionWidget(
                        text("mapColor"),
                        description("pathFillColor"),
                        OptionCategory.AREA_BORDER,
                        MapAttributes::getPathFillColor,
                        this,
                        false),
                MapAttributesBuilder::setPathFillColor,
                own,
                inherited);
        bind(
                mapOptions,
                new ColorOptionWidget(
                        text("borderColorShort"),
                        description("pathBorderColor"),
                        OptionCategory.AREA_BORDER,
                        MapAttributes::getPathBorderColor,
                        this,
                        false),
                MapAttributesBuilder::setPathBorderColor,
                own,
                inherited);
        bind(
                mapOptions,
                new FloatSliderOptionWidget(
                        text("pathWidth"),
                        description("pathWidth"),
                        OptionCategory.AREA_BORDER,
                        0f,
                        20f,
                        MapAttributes::getPathWidth,
                        this),
                MapAttributesBuilder::setPathWidth,
                own,
                inherited);
        bind(
                mapOptions,
                new FloatSliderOptionWidget(
                        text("borderWidth"),
                        description("pathBorderWidth"),
                        OptionCategory.AREA_BORDER,
                        0f,
                        10f,
                        MapAttributes::getPathBorderWidth,
                        this),
                MapAttributesBuilder::setPathBorderWidth,
                own,
                inherited);
        bind(
                mapOptions,
                new ColorOptionWidget(
                        text("labelColor"),
                        description("labelColor"),
                        OptionCategory.LABEL,
                        MapAttributes::getLabelColor,
                        this,
                        true),
                MapAttributesBuilder::setLabelColor,
                own,
                inherited);
        bind(
                mapOptions,
                new TextShadowOptionWidget(
                        text("shadow"),
                        description("labelShadow"),
                        OptionCategory.LABEL,
                        MapAttributes::getLabelShadow),
                MapAttributesBuilder::setLabelShadow,
                own,
                inherited);

        bind(
                mapOptions,
                new VisiblityOptionWidget(
                        Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibility"),
                        description("labelVisibility"),
                        OptionCategory.LABEL,
                        DefaultMapAttributes.LABEL_NEVER,
                        DefaultMapAttributes.LABEL_ALWAYS,
                        Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityMin"),
                        description("labelVisibilityMin"),
                        Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityMax"),
                        description("labelVisibilityMax"),
                        Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityFade"),
                        description("labelVisibilityFade"),
                        MapAttributes::getLabelVisibility,
                        this,
                        mapOptions::updateWidgetPositions),
                (builder, visibility) ->
                        builder.setLabelVisibility(visibility == null ? null : new MapVisibilityImpl(visibility)),
                own,
                inherited);

        int actionWidth = (546 - 5) / 2;

        button(left, y + 282, actionWidth, "cancel", Texture.MANAGER_WIDGET_BACKGROUND_RED, () -> {
            Services.MapPaths.cancel();
            McUtils.setScreen(previous);
        });

        int saveX = left + actionWidth + 5;
        saveButton = null;
        if (editing || Services.MapPaths.isRecording()) {
            saveButton =
                    button(saveX, y + 282, left + 546 - saveX, "save", Texture.MANAGER_WIDGET_BACKGROUND_GREEN, () -> {
                        configure();
                        MapPathsService.SaveResult result = Services.MapPaths.save(label);
                        if (result != MapPathsService.SaveResult.SAVED) {
                            error = result.getMessage().getString();
                            McUtils.sendWynntilsPrefixMessage(
                                    result.getMessage().withStyle(ChatFormatting.RED));

                            return;
                        }

                        McUtils.setScreen(previous);
                    });
            saveButton.setTooltip(Tooltip.create(text("save.tooltip")));
        } else {
            button(saveX, y + 282, left + 546 - saveX, "record", Texture.MANAGER_WIDGET_BACKGROUND_GREEN, () -> {
                configure();
                if (Services.MapPaths.startRecording()) {
                    McUtils.setScreen(null);
                }
            });
        }

        updateButtons();
    }

    @Override
    public void onClose() {
        if (editing) {
            Services.MapPaths.cancel();
        } else {
            configure();
        }

        McUtils.setScreen(previous);
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(graphics, Texture.MANAGER_BACKGROUND, x, y);
        RenderUtils.drawNineSliceScalingTexturedRect(
                graphics, Texture.MANAGER_SCROLL_BAR_HEADER, x + 15, y + 18, 150, 16);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(getTitle()),
                        x + 35,
                        y + 26,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(text("label")),
                        x + 15,
                        y + 39,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(text("category")),
                        x + 290,
                        y + 39,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        updateButtons();
        renderables.forEach(widget -> widget.render(graphics, mouseX, mouseY, partialTick));
        if (!error.isBlank()) {
            FontRenderer.getInstance()
                    .renderScrollingText(
                            graphics,
                            StyledText.fromComponent(text("error", error)),
                            x + INFO_BUTTON_X - 8,
                            y + 26,
                            INFO_BUTTON_X - 8 - 196,
                            CommonColors.RED,
                            HorizontalAlignment.RIGHT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        if (infoButton.isMouseOver(mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(
                    infoButton.getTooltipLines().stream()
                            .map(Component::getVisualOrderText)
                            .toList(),
                    mouseX,
                    mouseY);
        }

        for (PathOptionsScrollBarWidget panel : List.of(worldOptions, mapOptions)) {
            List<Component> lines = panel.getTooltipLines(mouseX, mouseY);
            if (!lines.isEmpty()) {
                graphics.setTooltipForNextFrame(
                        lines.stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        for (PathOptionsScrollBarWidget panel : List.of(worldOptions, mapOptions)) {
            if (!panel.isInsideViewport(event.x(), event.y())) {
                continue;
            }

            for (AbstractOptionWidget<?> widget : panel.getWidgets()) {
                if (widget.visible && widget.isMouseOverTextBox(event.x(), event.y())) {
                    setFocusedTextInput(widget.getTextInputBoxWidget(event.x(), event.y()));
                }
            }
        }

        boolean handled = super.doMouseClicked(event, isDoubleClick);
        TextInputBoxWidget focused = getFocusedTextInput();
        if (focused != null && !focused.isMouseOver(event.x(), event.y())) {
            setFocusedTextInput(null);
        }

        return handled;
    }

    @Override
    protected void rebuildWidgets() {
        collectOptions();
        super.rebuildWidgets();
    }

    private MapPathAttributesImpl inheritedStyle() {
        ResolvedMapAttributes attributes = MapAttributesResolver.resolve(
                new MapPathImpl("preview:path", category, null, List.of(Vec3.ZERO, new Vec3(1, 0, 0))));

        return new MapAttributesBuilder()
                .setPathColor(attributes.pathColor())
                .setPathIcon(attributes.pathIconId())
                .setHasPath(attributes.hasPath())
                .setPathFillColor(attributes.pathFillColor())
                .setPathBorderColor(attributes.pathBorderColor())
                .setPathWidth(attributes.pathWidth())
                .setPathBorderWidth(attributes.pathBorderWidth())
                .setLabelVisibility(new MapVisibilityImpl(
                        attributes.labelVisibility().min(),
                        attributes.labelVisibility().max(),
                        attributes.labelVisibility().fade()))
                .setLabelColor(attributes.labelColor())
                .setLabelShadow(attributes.labelShadow())
                .asPathAttributes()
                .build();
    }

    private <T> void bind(
            PathOptionsScrollBarWidget panel,
            AbstractOptionWidget<T> widget,
            BiConsumer<MapAttributesBuilder, T> setter,
            MapAttributes own,
            MapAttributes inherited) {
        widget.updateFromAttributes(Optional.of(own), Optional.of(inherited));
        bindings.add(new OptionBinding<>(widget, setter));
        panel.addWidget(widget);
    }

    private void collectOptions() {
        for (OptionBinding<?> binding : bindings) {
            binding.apply(settings);
        }

        if (recordChests != null) {
            recordHighlights = recordChests.getValue();
        }
    }

    private void configure() {
        collectOptions();
        Services.MapPaths.configure(category, label, settings.asPathAttributes().build(), recordHighlights);
    }

    private StyledButton button(int x, int y, int width, String key, Texture background, Runnable action) {
        Texture inactive = background == Texture.MANAGER_WIDGET_BACKGROUND_GREEN
                ? Texture.MANAGER_WIDGET_BACKGROUND_DARKGREEN
                : background;

        return addRenderableWidget(new StyledButton(x, y, width, 20, text(key), background, inactive, action));
    }

    private void updateButtons() {
        if (saveButton == null) return;

        boolean canSave = Services.MapPaths.hasDraft() && Services.MapPaths.getPointCount() >= 2;
        if (saveButton.active == canSave) return;

        saveButton.active = canSave;
        saveButton.setTooltip(Tooltip.create(
                canSave ? text("save.tooltip") : MapPathsService.SaveResult.TOO_FEW_POINTS.getMessage()));
    }

    private Component text(String key, Object... arguments) {
        return Component.translatable("screens.wynntils.map.paths." + key, arguments);
    }

    private Component description(String key) {
        return Component.translatable("screens.wynntils.map.managers.categoryManager." + key + ".description");
    }

    private record OptionBinding<T>(AbstractOptionWidget<T> widget, BiConsumer<MapAttributesBuilder, T> setter) {
        void apply(MapAttributesBuilder settings) {
            if (widget.isChanged()) {
                setter.accept(settings, widget.isOverridden() ? widget.getValue() : null);
            }
        }
    }
}
