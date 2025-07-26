/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.RarityStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public class RarityFilterWidget extends GuideFilterWidget {
    private final List<RarityButton> rarityButtons = new ArrayList<>();
    private RarityStatProvider provider;

    public RarityFilterWidget(int x, int y, WynntilsGuideScreen guideScreen, ItemSearchQuery searchQuery) {
        super(x, y, 136, 16, guideScreen);

        rarityButtons.add(new RarityButton(x, y, GearTier.MYTHIC, Texture.MYTHIC_FILTER_ICON, searchQuery));
        rarityButtons.add(new RarityButton(x + 20, y, GearTier.FABLED, Texture.FABLED_FILTER_ICON, searchQuery));
        rarityButtons.add(new RarityButton(x + 40, y, GearTier.LEGENDARY, Texture.LEGENDARY_FILTER_ICON, searchQuery));
        rarityButtons.add(new RarityButton(x + 60, y, GearTier.SET, Texture.SET_FILTER_ICON, searchQuery));
        rarityButtons.add(new RarityButton(x + 80, y, GearTier.RARE, Texture.RARE_FILTER_ICON, searchQuery));
        rarityButtons.add(new RarityButton(x + 100, y, GearTier.UNIQUE, Texture.UNIQUE_FILTER_ICON, searchQuery));
        rarityButtons.add(new RarityButton(x + 120, y, GearTier.NORMAL, Texture.NORMAL_FILTER_ICON, searchQuery));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        rarityButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = false;

        for (RarityButton rarityButton : rarityButtons) {
            if (rarityButton.isMouseOver(mouseX, mouseY)) {
                clicked = rarityButton.mouseClicked(mouseX, mouseY, button);
                break;
            }
        }

        guideScreen.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        List<StatProviderAndFilterPair> filterPairs = new ArrayList<>();

        for (RarityButton rarityButton : rarityButtons) {
            StatProviderAndFilterPair filterPair = rarityButton.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public void getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof RarityStatProvider)
                .map(statProvider -> (RarityStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get rarity stat provider"));
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        rarityButtons.forEach(classTypeButton -> classTypeButton.updateStateFromQuery(searchQuery));
    }

    private static class RarityButton extends GuideFilterButton<RarityStatProvider> {
        private final GearTier gearTier;

        protected RarityButton(int x, int y, GearTier gearTier, Texture texture, ItemSearchQuery searchQuery) {
            super(x, y, texture);

            this.gearTier = gearTier;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            state = searchQuery.filters().values().stream()
                    .filter(filterPair -> filterPair.statProvider() instanceof RarityStatProvider)
                    .anyMatch(filterPair -> filterPair.statFilter().matches(gearTier.getName()));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(RarityStatProvider provider) {
            if (!state) return null;

            Optional<StringStatFilter> statFilterOpt =
                    new StringStatFilter.StringStatFilterFactory().create(gearTier.getName());

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }

        @Override
        protected String getFilterName() {
            return I18n.get("service.wynntils.itemFilter.stat.rarity.name") + " " + gearTier.getName();
        }
    }
}
