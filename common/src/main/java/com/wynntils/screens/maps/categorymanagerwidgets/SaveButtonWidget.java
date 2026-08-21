package com.wynntils.screens.maps.categorymanagerwidgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.CategoryManagementScreen;
import com.wynntils.screens.maps.type.OverrideType;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.impl.MapAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapMarkerOptionsImpl;
import com.wynntils.services.mapdata.attributes.impl.MapVisibilityImpl;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.providers.json.JsonOverrideProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class SaveButtonWidget extends AbstractWidget {
    private final int x;
    private final int y;
    private final CategoryManagementScreen parent;

    public SaveButtonWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, Component.literal("Save Button Widget"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                x,
                y,
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString("Save"),
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

        String overrideName = parent.getSelectedOverrideType().name().toLowerCase(Locale.ROOT) + ":" + parent.getSelectedCategory();

        MapAttributesBuilder builder = new MapAttributesBuilder();
        Optional.ofNullable(Services.MapData.getOverrideProvider(
                        "json-override:" + overrideName))
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
        boolean labelVisibilityInherited = parent.labelVisibilityMinOptionWidget.isChanged()
                || parent.labelVisibilityMaxOptionWidget.isChanged()
                || parent.labelVisibilityFadeOptionWidget.isChanged();

        MapVisibilityImpl labelVisibilityValue = new MapVisibilityImpl(
                parent.labelVisibilityMinOptionWidget.getValue(),
                parent.labelVisibilityMaxOptionWidget.getValue(),
                parent.labelVisibilityFadeOptionWidget.getValue());

        buildOption(
                builder,
                labelVisibilityInherited,
                labelVisibilityValue,
                MapAttributesBuilder::setLabelVisibility);

        // iconVisibility
        boolean iconVisibilityInherited = parent.iconVisibilityMinOptionWidget.isChanged()
                || parent.iconVisibilityMaxOptionWidget.isChanged()
                || parent.iconVisibilityFadeOptionWidget.isChanged();

        MapVisibilityImpl iconVisibilityValue = new MapVisibilityImpl(
                parent.iconVisibilityMinOptionWidget.getValue(),
                parent.iconVisibilityMaxOptionWidget.getValue(),
                parent.iconVisibilityFadeOptionWidget.getValue());

        buildOption(
                builder,
                iconVisibilityInherited,
                iconVisibilityValue,
                MapAttributesBuilder::setIconVisibility);

    // markerOptions
        boolean markerOptionsInherited = parent.markerMinDistanceOptionWidget.isChanged()
                || parent.markerMaxDistanceOptionWidget.isChanged()
                || parent.markerFadeOptionWidget.isChanged()
                || parent.markerBeaconColorOptionWidget.isChanged()
                || parent.markerHasLabelOptionWidget.isChanged()
                || parent.markerHasDistanceLabelOptionWidget.isChanged()
                || parent.markerHasIconOptionWidget.isChanged();

        MapMarkerOptionsImpl markerOptionsValue = new MapMarkerOptionsImpl(
                parent.markerMinDistanceOptionWidget.getValue(),
                parent.markerMaxDistanceOptionWidget.getValue(),
                parent.markerFadeOptionWidget.getValue(),
                parent.markerBeaconColorOptionWidget.getValue(),
                parent.markerHasLabelOptionWidget.getValue(),
                parent.markerHasDistanceLabelOptionWidget.getValue(),
                parent.markerHasIconOptionWidget.getValue());

        buildOption(
                builder,
                markerOptionsInherited,
                markerOptionsValue,
                MapAttributesBuilder::setMarkerOptions);

        MapAttributesImpl attributes = switch (parent.getSelectedOverrideType()) {
            case MAP_LOCATION_OVERRIDE -> builder.asLocationAttributes().build();
            case MAP_PATH_OVERRIDE -> builder.asPathAttributes().build();
            case MAP_AREA_OVERRIDE -> builder.asAreaAttributes().build();
        };

        Services.MapData.addOverrideProvider(new JsonOverrideProvider(
                overrideName,
                attributes,
                Set.of(),
                Set.of(parent.getSelectedCategory())));

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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
