/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.AnimationPercentage;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class FilterHolderWidget extends AbstractWidget {
    private final Set<String> categories;
    private List<FilteredMarkerWidget> filteredMarkerWidgets = new ArrayList<>();

    private final AnimationPercentage animationPercentage;
    private final FilterOpeningButton openingButton;
    private final NavigationButton nextButton;
    private final NavigationButton previousButton;
    private final int originalY;
    private final double scaleFactor;
    private final int maxWidgets;

    private boolean opened;
    private int widgetStartX;
    private int markersScrollOffset = 0;

    public FilterHolderWidget(int x, int y, int width, int height, double scaleFactor, boolean opened) {
        super(x, y, width, height, Component.empty());

        this.originalY = y;
        this.opened = opened;
        this.scaleFactor = scaleFactor;
        animationPercentage = new AnimationPercentage(this::isOpen, Duration.of(500, ChronoUnit.MILLIS));
        animationPercentage.setOpeningProgress(opened ? 1 : 0);

        openingButton = new FilterOpeningButton(
                (int) (x + width - (Texture.BUTTON_BOTTOM.width() * scaleFactor) - (4 * scaleFactor)),
                (int) (y + getHeight() - (4 * scaleFactor)),
                (int) (Texture.BUTTON_BOTTOM.width() * scaleFactor),
                (int) (Texture.BUTTON_BOTTOM.height() / 2f * scaleFactor),
                this);

        categories = Stream.concat(
                        Services.MapData.getFeaturesForCategory("wynntils:service:"),
                        Services.MapData.getFeaturesForCategory("wynntils:content:"))
                .map(MapFeature::getCategoryId)
                .collect(Collectors.toSet());

        int spaceForWidgets = (int) (((Texture.MAP_FILTER_BACKGROUND.width() - 20) * scaleFactor) - 32 * scaleFactor);
        maxWidgets = (int) (spaceForWidgets / (16 * scaleFactor));
        int offset = (int) (spaceForWidgets - (maxWidgets * 16 * scaleFactor));

        widgetStartX = (int) (getX() + 10 * scaleFactor);
        int navigationButtonsY = (int) ((getY() + (18 * scaleFactor)));

        previousButton = new NavigationButton(
                widgetStartX, navigationButtonsY, (int) (16 * scaleFactor), (int) (32 * scaleFactor), this, false);
        nextButton = new NavigationButton(
                (int) (widgetStartX + ((Texture.MAP_FILTER_BACKGROUND.width() - 20) * scaleFactor) - 16 * scaleFactor),
                navigationButtonsY,
                (int) (16 * scaleFactor),
                (int) (32 * scaleFactor),
                this,
                true);

        widgetStartX += (int) (16 * scaleFactor) + offset;

        populateMarkerWidgets();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.setY(originalY + (int) (getHeight() * animationPercentage.getAnimation()));

        openingButton.render(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics.pose(),
                Texture.MAP_FILTER_BACKGROUND.resource(),
                getX(),
                getY(),
                100,
                getWidth(),
                getHeight(),
                Texture.MAP_FILTER_BACKGROUND.width(),
                Texture.MAP_FILTER_BACKGROUND.height());

        if (previousButton != null && nextButton != null) {
            previousButton.render(guiGraphics, mouseX, mouseY, partialTick);
            nextButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        filteredMarkerWidgets.forEach(widget -> widget.renderWidget(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        int increment = (int) (getHeight() * animationPercentage.getAnimation());

        openingButton.setY(openingButton.getOriginalY() + increment);
        previousButton.setY(previousButton.getOriginalY() + increment);
        nextButton.setY(nextButton.getOriginalY() + increment);

        for (FilteredMarkerWidget widget : filteredMarkerWidgets) {
            widget.setY(widget.getOriginalY() + increment);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return openingButton.isMouseOver(mouseX, mouseY)
                || this.active
                        && this.visible
                        && mouseX >= (double) this.getX()
                        && mouseY >= (double) this.getY()
                        && mouseX < (double) this.getRight()
                        && mouseY < (double) this.getBottom();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (openingButton.isMouseOver(mouseX, mouseY)) {
            return openingButton.mouseClicked(mouseX, mouseY, button);
        } else if (previousButton.isMouseOver(mouseX, mouseY)) {
            return previousButton.mouseClicked(mouseX, mouseY, button);
        } else if (nextButton.isMouseOver(mouseX, mouseY)) {
            return nextButton.mouseClicked(mouseX, mouseY, button);
        }

        for (FilteredMarkerWidget markerWidget : filteredMarkerWidgets) {
            if (markerWidget.isMouseOver(mouseX, mouseY)) {
                return markerWidget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return false;
    }

    private void populateMarkerWidgets() {
        filteredMarkerWidgets = new ArrayList<>();

        int xOffset = 0;

        List<String> categoryList = new ArrayList<>(categories);

        int categoryIndex;
        for (int i = 0; i < Math.min(maxWidgets, categoryList.size()); i++) {
            categoryIndex = (markersScrollOffset + i) % categories.size();
            String category = categoryList.get(categoryIndex);

            Optional<MapFeature> mapFeatureOpt =
                    Services.MapData.getFeaturesForCategory(category).findFirst();

            if (mapFeatureOpt.isPresent()) {
                ResolvedMapAttributes resolvedMapAttributes =
                        Services.MapData.resolveMapAttributes(mapFeatureOpt.get());
                Optional<MapIcon> mapIconOpt = Services.MapData.getIcon(resolvedMapAttributes.iconId());

                if (mapIconOpt.isPresent()) {
                    filteredMarkerWidgets.add(new FilteredMarkerWidget(
                            widgetStartX + xOffset,
                            (int) (originalY + (20 * scaleFactor)),
                            (int) (16 * scaleFactor),
                            (int) (32 * scaleFactor),
                            mapIconOpt.get(),
                            category));
                    xOffset += (int) (16 * scaleFactor);
                } else {
                    WynntilsMod.warn("Unable to find icon with id " + resolvedMapAttributes.iconId());
                }
            } else {
                WynntilsMod.warn("Unable to find mapfeatures for category " + category);
            }
        }
    }

    private void scrollMarkers(int direction) {
        if (direction == 1) {
            if (markersScrollOffset + 1 >= categories.size()) {
                markersScrollOffset = 0;
            } else {
                markersScrollOffset++;
            }
        } else {
            if (markersScrollOffset - 1 < 0) {
                markersScrollOffset = categories.size() - 1;
            } else {
                markersScrollOffset--;
            }
        }

        populateMarkerWidgets();
    }

    private void toggleOpened() {
        opened = !opened;
    }

    public boolean isOpen() {
        return opened;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private static final class FilterOpeningButton extends BasicTexturedButton {
        private final FilterHolderWidget parent;
        private final int originalY;

        private FilterOpeningButton(int x, int y, int width, int height, FilterHolderWidget parent) {
            super(x, y, width, height, Texture.BUTTON_BOTTOM, (b) -> {}, List.of());

            this.originalY = y;
            this.parent = parent;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawHoverableScalingTexturedRect(
                    guiGraphics.pose(),
                    Texture.BUTTON_BOTTOM,
                    getX(),
                    getY(),
                    100,
                    getWidth(),
                    getHeight(),
                    this.isHovered);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics.pose(),
                            StyledText.fromComponent(Component.translatable("screens.wynntils.map.configureMarkers")),
                            getX(),
                            getX() + getWidth(),
                            getY(),
                            getY() + getHeight() - 2,
                            getWidth(),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOver(mouseX, mouseY)) return false;

            parent.toggleOpened();
            return true;
        }

        public int getOriginalY() {
            return originalY;
        }
    }

    private static final class NavigationButton extends WynntilsButton {
        private final FilterHolderWidget parent;
        private final int originalY;
        private final boolean next;

        private NavigationButton(int x, int y, int width, int height, FilterHolderWidget parent, boolean next) {
            super(x, y, width, height, Component.empty());

            this.originalY = y;
            this.parent = parent;
            this.next = next;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawRect(
                    guiGraphics.pose(),
                    CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f),
                    getX(),
                    getY(),
                    0,
                    width,
                    height);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics.pose(),
                            StyledText.fromString(next ? ">" : "<"),
                            getX(),
                            getX() + getWidth(),
                            getY(),
                            getY() + getHeight(),
                            getWidth(),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        @Override
        public void onPress() {
            parent.scrollMarkers(next ? 1 : -1);
        }

        public int getOriginalY() {
            return originalY;
        }
    }
}
