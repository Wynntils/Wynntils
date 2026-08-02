/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class GuideFilterWidget extends AbstractWidget {
    protected final GuideContainerWidget<?> containerWidget;

    protected GuideFilterWidget(int height, GuideContainerWidget<?> containerWidget) {
        super(0, 0, 128, height, Component.empty());

        this.containerWidget = containerWidget;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        updateWidgetPositions();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        updateWidgetPositions();
    }

    public final String getItemSearchQuery() {
        Map<ItemStatProvider<?>, List<StatProviderAndFilterPair>> filterMap = new HashMap<>();

        for (StatProviderAndFilterPair filter : getFilters()) {
            filterMap
                    .computeIfAbsent(filter.statProvider(), k -> new ArrayList<>())
                    .add(filter);
        }

        return Services.ItemFilter.getItemFilterString(filterMap, List.of(), List.of());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        containerWidget.updateSearchFromQuickFilters();

        return false;
    }

    public abstract void updateFromQuery(ItemSearchQuery searchQuery);

    protected abstract void rebuildWidgets(ItemSearchQuery searchQuery);

    protected abstract void updateWidgetPositions();

    protected abstract List<StatProviderAndFilterPair> getFilters();

    public abstract ItemStatProvider<?> getProvider();

    public List<ItemStatProvider<?>> getProviders() {
        return List.of(getProvider());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
