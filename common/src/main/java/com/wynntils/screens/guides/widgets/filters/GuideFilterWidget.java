/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class GuideFilterWidget extends AbstractWidget {
    protected final WynntilsGuideScreen guideScreen;

    protected GuideFilterWidget(int x, int y, int width, int height, WynntilsGuideScreen guideScreen) {
        super(x, y, width, height, Component.empty());

        this.guideScreen = guideScreen;

        getProvider();
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        guideScreen.updateSearchFromQuickFilters();

        return false;
    }

    public abstract void updateFromQuery(ItemSearchQuery searchQuery);

    protected abstract List<StatProviderAndFilterPair> getFilters();

    protected abstract void getProvider();

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
