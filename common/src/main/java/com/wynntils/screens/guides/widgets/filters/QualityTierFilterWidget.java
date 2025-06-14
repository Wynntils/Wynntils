/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.statproviders.QualityTierStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public class QualityTierFilterWidget extends GuideFilterWidget {
    private final List<QualityTierButton> qualityTierButtons = new ArrayList<>();
    private QualityTierStatProvider provider;

    public QualityTierFilterWidget(int x, int y, WynntilsGuideScreen guideScreen, ItemSearchQuery searchQuery) {
        super(x, y, 76, 16, guideScreen);

        qualityTierButtons.add(new QualityTierButton(x, y, 0, Texture.TIER_0_FILTER_ICON, searchQuery));
        qualityTierButtons.add(new QualityTierButton(x + 20, y, 1, Texture.TIER_1_FILTER_ICON, searchQuery));
        qualityTierButtons.add(new QualityTierButton(x + 40, y, 2, Texture.TIER_2_FILTER_ICON, searchQuery));
        qualityTierButtons.add(new QualityTierButton(x + 60, y, 3, Texture.TIER_3_FILTER_ICON, searchQuery));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        qualityTierButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = false;

        for (QualityTierButton qualityTierButton : qualityTierButtons) {
            if (qualityTierButton.isMouseOver(mouseX, mouseY)) {
                clicked = qualityTierButton.mouseClicked(mouseX, mouseY, button);
                break;
            }
        }

        guideScreen.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        List<StatProviderAndFilterPair> filterPairs = new ArrayList<>();

        for (QualityTierButton qualityTierButton : qualityTierButtons) {
            StatProviderAndFilterPair filterPair = qualityTierButton.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public void getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof QualityTierStatProvider)
                .map(statProvider -> (QualityTierStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get quality tier stat provider"));
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        qualityTierButtons.forEach(classTypeButton -> classTypeButton.updateStateFromQuery(searchQuery));
    }

    private static class QualityTierButton extends GuideFilterButton<QualityTierStatProvider> {
        private final int tier;

        protected QualityTierButton(int x, int y, int tier, Texture texture, ItemSearchQuery searchQuery) {
            super(x, y, texture);

            this.tier = tier;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            state = searchQuery.filters().values().stream()
                    .filter(filterPair -> filterPair.statProvider() instanceof QualityTierStatProvider)
                    .anyMatch(filterPair -> filterPair.statFilter().matches(tier));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(QualityTierStatProvider provider) {
            if (!state) return null;

            Optional<RangedStatFilters.RangedIntegerStatFilter> statFilterOpt =
                    new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory()
                            .create(String.valueOf(tier));

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }

        @Override
        protected String getFilterName() {
            return I18n.get("service.wynntils.itemFilter.stat.qualityTier.name") + " " + tier;
        }
    }
}
