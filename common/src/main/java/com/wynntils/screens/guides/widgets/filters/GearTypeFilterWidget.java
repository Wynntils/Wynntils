/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.models.gear.type.GearType;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.GearTypeStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public class GearTypeFilterWidget extends GuideFilterWidget {
    private final List<GearTypeButton> gearTypeButtons = new ArrayList<>();

    public GearTypeFilterWidget(int x, int y, WynntilsGuideScreen guideScreen, ItemSearchQuery searchQuery) {
        super(x, y, 144, 36, guideScreen);

        gearTypeButtons.add(new GearTypeButton(x + 16, y, GearType.HELMET, Texture.HELMET_FILTER_ICON, searchQuery));
        gearTypeButtons.add(
                new GearTypeButton(x + 48, y, GearType.CHESTPLATE, Texture.CHESTPLATE_FILTER_ICON, searchQuery));
        gearTypeButtons.add(
                new GearTypeButton(x + 80, y, GearType.LEGGINGS, Texture.LEGGINGS_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(x + 112, y, GearType.BOOTS, Texture.BOOTS_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(x, y + 20, GearType.SPEAR, Texture.SPEAR_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(x + 32, y + 20, GearType.WAND, Texture.WAND_FILTER_ICON, searchQuery));
        gearTypeButtons.add(
                new GearTypeButton(x + 64, y + 20, GearType.DAGGER, Texture.DAGGER_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(x + 96, y + 20, GearType.BOW, Texture.BOW_FILTER_ICON, searchQuery));
        gearTypeButtons.add(
                new GearTypeButton(x + 128, y + 20, GearType.RELIK, Texture.RELIK_FILTER_ICON, searchQuery));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        gearTypeButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = false;

        for (GearTypeButton gearTypeButton : gearTypeButtons) {
            if (gearTypeButton.isMouseOver(mouseX, mouseY)) {
                clicked = gearTypeButton.mouseClicked(mouseX, mouseY, button);
                break;
            }
        }

        guideScreen.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        List<StatProviderAndFilterPair> filterPairs = new ArrayList<>();
        GearTypeStatProvider gearTypeStatProvider = new GearTypeStatProvider();

        for (GearTypeButton gearTypeButton : gearTypeButtons) {
            StatProviderAndFilterPair filterPair = gearTypeButton.getFilterPair(gearTypeStatProvider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        gearTypeButtons.forEach(classTypeButton -> classTypeButton.updateStateFromQuery(searchQuery));
    }

    private static class GearTypeButton extends GuideFilterButton<GearTypeStatProvider> {
        private final GearType gearType;

        protected GearTypeButton(int x, int y, GearType gearType, Texture texture, ItemSearchQuery searchQuery) {
            super(x, y, texture);

            this.gearType = gearType;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            state = searchQuery.filters().values().stream()
                    .filter(filterPair -> filterPair.statProvider() instanceof GearTypeStatProvider)
                    .anyMatch(filterPair -> filterPair.statFilter().matches(EnumUtils.toNiceString(gearType)));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(GearTypeStatProvider provider) {
            if (!state) return null;

            Optional<StringStatFilter> statFilterOpt =
                    new StringStatFilter.StringStatFilterFactory().create(EnumUtils.toNiceString(gearType));

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }

        @Override
        protected String getFilterName() {
            return I18n.get("service.wynntils.itemFilter.stat.gearType.name") + " " + EnumUtils.toNiceString(gearType);
        }
    }
}
