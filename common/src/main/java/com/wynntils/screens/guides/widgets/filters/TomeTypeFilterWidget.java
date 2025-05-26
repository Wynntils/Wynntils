/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.models.rewards.TomeType;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.TomeTypeStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public class TomeTypeFilterWidget extends GuideFilterWidget {
    private final List<TomeTypeButton> tomeTypeButtons = new ArrayList<>();
    private TomeTypeStatProvider provider;

    public TomeTypeFilterWidget(int x, int y, WynntilsGuideScreen guideScreen, ItemSearchQuery searchQuery) {
        super(x, y, 136, 16, guideScreen);

        tomeTypeButtons.add(new TomeTypeButton(x, y, TomeType.GUILD_TOME, Texture.GUILD_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(
                new TomeTypeButton(x + 20, y, TomeType.WEAPON_TOME, Texture.WEAPON_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(
                new TomeTypeButton(x + 40, y, TomeType.ARMOUR_TOME, Texture.ARMOR_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(new TomeTypeButton(
                x + 60, y, TomeType.MYSTICISM_TOME, Texture.MYSTICISM_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(
                new TomeTypeButton(x + 80, y, TomeType.MARATHON_TOME, Texture.MARATHON_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(
                new TomeTypeButton(x + 100, y, TomeType.LOOTRUN_TOME, Texture.LOOTRUN_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(new TomeTypeButton(
                x + 120, y, TomeType.EXPERTISE_TOME, Texture.EXPERTISE_TOME_FILTER_ICON, searchQuery));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        tomeTypeButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = false;

        for (TomeTypeButton tomeTypeButton : tomeTypeButtons) {
            if (tomeTypeButton.isMouseOver(mouseX, mouseY)) {
                clicked = tomeTypeButton.mouseClicked(mouseX, mouseY, button);
                break;
            }
        }

        guideScreen.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        List<StatProviderAndFilterPair> filterPairs = new ArrayList<>();

        for (TomeTypeButton tomeTypeButton : tomeTypeButtons) {
            StatProviderAndFilterPair filterPair = tomeTypeButton.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public void getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof TomeTypeStatProvider)
                .map(statProvider -> (TomeTypeStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get tome type stat provider"));
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        tomeTypeButtons.forEach(classTypeButton -> classTypeButton.updateStateFromQuery(searchQuery));
    }

    private static class TomeTypeButton extends GuideFilterButton<TomeTypeStatProvider> {
        private final TomeType tomeType;

        protected TomeTypeButton(int x, int y, TomeType tomeType, Texture texture, ItemSearchQuery searchQuery) {
            super(x, y, texture);

            this.tomeType = tomeType;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            state = searchQuery.filters().values().stream()
                    .filter(filterPair -> filterPair.statProvider() instanceof TomeTypeStatProvider)
                    .anyMatch(filterPair -> filterPair
                            .statFilter()
                            .matches(EnumUtils.toNiceString(tomeType).replace(" ", "_")));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(TomeTypeStatProvider provider) {
            if (!state) return null;

            Optional<StringStatFilter> statFilterOpt = new StringStatFilter.StringStatFilterFactory()
                    .create(EnumUtils.toNiceString(tomeType).replace(" ", "_"));

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }

        @Override
        protected String getFilterName() {
            return I18n.get("service.wynntils.itemFilter.stat.tomeType.name") + " " + EnumUtils.toNiceString(tomeType);
        }
    }
}
