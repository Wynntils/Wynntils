/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.type.OverrideType;
import com.wynntils.screens.maps.managers.widgets.options.AbstractOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.ColorOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.FloatSliderOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.ToggleOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.VisiblityOptionWidget;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.impl.MapAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapMarkerOptionsImpl;
import com.wynntils.services.mapdata.attributes.impl.MapVisibilityImpl;
import com.wynntils.services.mapdata.providers.json.JsonOverrideProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SaveButtonWidget extends AbstractWidget implements TooltipProvider {
    private final int x;
    private final int y;
    private final CategoryManagementScreen parent;
    private List<Component> generatedTooltip = new ArrayList<>();

    public SaveButtonWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, Component.literal("Save Button Widget"));
        this.x = x;
        this.y = y;
        this.parent = parent;
        generateTooltip();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND_GREEN, x, y, this.width, this.height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.saveButton.label")),
                        x + this.width / 2f,
                        y + this.height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        MapAttributesBuilder builder = new MapAttributesBuilder();
        Optional.ofNullable(Services.MapData.getOverrideProvider(parent.getOverrideName(true)))
                .ifPresent(provider -> builder.from(provider.getOverrideAttributes(null)));

        buildOption(
                builder,
                parent.priorityOptionWidget.isChanged(),
                parent.priorityOptionWidget.getValue(),
                MapAttributesBuilder::setPriority);

        buildOption(
                builder,
                parent.levelOptionWidget.isChanged(),
                parent.levelOptionWidget.getValue(),
                MapAttributesBuilder::setLevel);

        buildOption(
                builder,
                parent.labelOptionWidget.isChanged(),
                parent.labelOptionWidget.getValue(),
                MapAttributesBuilder::setLabel);

        buildOption(
                builder,
                parent.descriptionOptionWidget.isChanged(),
                parent.descriptionOptionWidget.getValue(),
                MapAttributesBuilder::setDescription);

        buildOption(
                builder,
                parent.labelColorOptionWidget.isChanged(),
                parent.labelColorOptionWidget.getValue(),
                MapAttributesBuilder::setLabelColor);

        buildOption(
                builder,
                parent.labelShadowOptionWidget.isChanged(),
                parent.labelShadowOptionWidget.getValue(),
                MapAttributesBuilder::setLabelShadow);

        buildOption(
                builder,
                parent.iconOptionWidget.isChanged(),
                parent.iconOptionWidget.getValue(),
                MapAttributesBuilder::setIcon);

        buildOption(
                builder,
                parent.iconColorOptionWidget.isChanged(),
                parent.iconColorOptionWidget.getValue(),
                MapAttributesBuilder::setIconColor);

        buildOption(
                builder,
                parent.hasMarkerOptionWidget.isChanged(),
                parent.hasMarkerOptionWidget.getValue(),
                MapAttributesBuilder::setHasMarker);

        buildOption(
                builder,
                parent.fillColorOptionWidget.isChanged(),
                parent.fillColorOptionWidget.getValue(),
                MapAttributesBuilder::setFillColor);

        buildOption(
                builder,
                parent.borderColorOptionWidget.isChanged(),
                parent.borderColorOptionWidget.getValue(),
                MapAttributesBuilder::setBorderColor);

        buildOption(
                builder,
                parent.borderWidthOptionWidget.isChanged(),
                parent.borderWidthOptionWidget.getValue(),
                MapAttributesBuilder::setBorderWidth);

        // labelVisibility
        VisiblityOptionWidget labelVisibility = parent.labelVisibilityOptionWidget;

        buildOption(
                builder,
                labelVisibility.minSlider.isChanged()
                        || labelVisibility.maxSlider.isChanged()
                        || labelVisibility.fadeSlider.isChanged(),
                new MapVisibilityImpl(
                        valueOrNull(labelVisibility.minSlider),
                        valueOrNull(labelVisibility.maxSlider),
                        valueOrNull(labelVisibility.fadeSlider)),
                MapAttributesBuilder::setLabelVisibility);

        // iconVisibility
        VisiblityOptionWidget iconVisibility = parent.iconVisibilityOptionWidget;

        buildOption(
                builder,
                iconVisibility.minSlider.isChanged()
                        || iconVisibility.maxSlider.isChanged()
                        || iconVisibility.fadeSlider.isChanged(),
                new MapVisibilityImpl(
                        valueOrNull(iconVisibility.minSlider),
                        valueOrNull(iconVisibility.maxSlider),
                        valueOrNull(iconVisibility.fadeSlider)),
                MapAttributesBuilder::setIconVisibility);

        // markerOptions
        FloatSliderOptionWidget markerMinDistance = parent.markerMinDistanceOptionWidget;
        FloatSliderOptionWidget markerMaxDistance = parent.markerMaxDistanceOptionWidget;
        FloatSliderOptionWidget markerFade = parent.markerFadeOptionWidget;
        ColorOptionWidget markerBeaconColor = parent.markerBeaconColorOptionWidget;
        ToggleOptionWidget markerHasLabel = parent.markerHasLabelOptionWidget;
        ToggleOptionWidget markerHasDistanceLabel = parent.markerHasDistanceLabelOptionWidget;
        ToggleOptionWidget markerHasIcon = parent.markerHasIconOptionWidget;

        boolean markerOptionsInherited = isChanged(markerMinDistance)
                || isChanged(markerMaxDistance)
                || isChanged(markerFade)
                || isChanged(markerBeaconColor)
                || isChanged(markerHasLabel)
                || isChanged(markerHasDistanceLabel)
                || isChanged(markerHasIcon);

        MapMarkerOptionsImpl markerOptionsValue = new MapMarkerOptionsImpl(
                valueOrNull(markerMinDistance),
                valueOrNull(markerMaxDistance),
                valueOrNull(markerFade),
                valueOrNull(markerBeaconColor),
                valueOrNull(markerHasLabel),
                valueOrNull(markerHasDistanceLabel),
                valueOrNull(markerHasIcon));

        buildOption(builder, markerOptionsInherited, markerOptionsValue, MapAttributesBuilder::setMarkerOptions);

        MapAttributesImpl attributes =
                switch (parent.getSelectedOverrideType()) {
                    case MAP_LOCATION_OVERRIDE -> builder.asLocationAttributes().build();
                    case MAP_PATH_OVERRIDE -> builder.asPathAttributes().build();
                    case MAP_AREA_OVERRIDE -> builder.asAreaAttributes().build();
                };

        Services.MapData.addOverrideProvider(new JsonOverrideProvider(
                parent.getOverrideName(false), attributes, Set.of(), Set.of(parent.getSelectedCategory())));

        parent.setSelectedCategory(parent.getSelectedCategory());

        return true;
    }

    private <T> void buildOption(
            MapAttributesBuilder builder,
            boolean isChanged,
            T widgetValue,
            BiConsumer<MapAttributesBuilder, T> setter) {
        if (!isChanged) return;

        setter.accept(builder, widgetValue);
    }

    private boolean isChanged(AbstractOptionWidget<?> widget) {
        return widget.isChanged(); // || !widget.isInherited();
    }

    private <T> T valueOrNull(AbstractOptionWidget<T> widget) {
        return isChanged(widget) ? widget.getValue() : null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }

    public void generateTooltip() {
        this.generatedTooltip = new ArrayList<>();

        this.generatedTooltip.add(
                Component.translatable("screens.wynntils.map.managers.categoryManager.saveButton.label")
                        .withStyle(ChatFormatting.GOLD));

        Component typeLabel = Component.literal(formatOverrideType(parent.getSelectedOverrideType()))
                .withStyle(ChatFormatting.YELLOW);
        this.generatedTooltip.add(
                Component.translatable("screens.wynntils.map.managers.categoryManager.saveButton.type")
                        .withStyle(ChatFormatting.GRAY)
                        .append(typeLabel));

        boolean overrideExists = Services.MapData.getOverrideProvider(parent.getOverrideName(true)) != null;

        Component actionLabel = (overrideExists
                        ? Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.saveButton.action.update")
                        : Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.saveButton.action.create"))
                .withStyle(overrideExists ? ChatFormatting.AQUA : ChatFormatting.GREEN);

        this.generatedTooltip.add(
                Component.translatable("screens.wynntils.map.managers.categoryManager.saveButton.action")
                        .withStyle(ChatFormatting.GRAY)
                        .append(actionLabel));

        this.generatedTooltip.add(Component.empty());

        Component description = (overrideExists
                ? Component.translatable("screens.wynntils.map.managers.categoryManager.saveButton.description.update")
                : Component.translatable(
                        "screens.wynntils.map.managers.categoryManager.saveButton.description.create"));

        for (StyledText line : RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(description), 210)) {
            this.generatedTooltip.add(
                    Component.empty().append(line.getComponent()).withStyle(ChatFormatting.GRAY));
        }
    }

    private static String formatOverrideType(OverrideType overrideType) {
        String[] words = overrideType.name().split("_");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            formatted
                    .append(word.charAt(0))
                    .append(word.substring(1).toLowerCase(Locale.ROOT))
                    .append(" ");
        }

        return formatted.toString().trim();
    }
}
