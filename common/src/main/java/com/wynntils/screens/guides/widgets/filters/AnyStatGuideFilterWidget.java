/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.AnyStatFilters;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class AnyStatGuideFilterWidget<T extends ItemStatProvider<String>> extends GuideFilterWidget {
    private final T provider;
    private GuideFilterCheckbox<T> checkbox;

    public AnyStatGuideFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery, T provider) {
        super(30, containerWidget);
        this.provider = provider;
        rebuildWidgets(searchQuery);
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal(provider.getDisplayName())
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        getWidth(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        checkbox.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        if (checkbox.isMouseOver(event.x(), event.y())) {
            clicked = checkbox.mouseClicked(event, isDoubleClick);
        }

        if (clicked) {
            containerWidget.updateSearchFromQuickFilters();
        }

        return clicked;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        checkbox = new GuideFilterCheckbox<>("Enabled") {
            @Override
            protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
                selected = hasProviderFilter(searchQuery);
            }

            @Override
            protected StatProviderAndFilterPair getFilterPair(T provider) {
                return null;
            }
        };
        updateFromQuery(searchQuery);
        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (checkbox != null) checkbox.setPosition(getX(), getY() + 10);
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        if (!checkbox.selected) return List.of();
        return List.of(new StatProviderAndFilterPair(provider, new AnyStatFilters.AnyStringStatFilter()));
    }

    @Override
    public void updateFromQuery(ItemSearchQuery searchQuery) {
        checkbox.updateStateFromQuery(searchQuery);
    }

    private boolean hasProviderFilter(ItemSearchQuery searchQuery) {
        return searchQuery.filters().values().stream()
                .anyMatch(filterPair -> filterPair.statProvider().getName().equals(provider.getName()));
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        return provider;
    }
}
