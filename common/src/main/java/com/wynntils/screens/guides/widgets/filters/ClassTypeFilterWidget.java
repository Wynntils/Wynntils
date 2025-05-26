/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.ClassStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public class ClassTypeFilterWidget extends GuideFilterWidget {
    private final List<ClassTypeButton> classTypeButtons = new ArrayList<>();
    private ClassStatProvider provider;

    public ClassTypeFilterWidget(int x, int y, WynntilsGuideScreen guideScreen, ItemSearchQuery searchQuery) {
        super(x, y, 96, 16, guideScreen);

        classTypeButtons.add(new ClassTypeButton(x, y, ClassType.WARRIOR, Texture.SPEAR_FILTER_ICON, searchQuery));
        classTypeButtons.add(new ClassTypeButton(x + 20, y, ClassType.MAGE, Texture.WAND_FILTER_ICON, searchQuery));
        classTypeButtons.add(
                new ClassTypeButton(x + 40, y, ClassType.ASSASSIN, Texture.DAGGER_FILTER_ICON, searchQuery));
        classTypeButtons.add(new ClassTypeButton(x + 60, y, ClassType.ARCHER, Texture.BOW_FILTER_ICON, searchQuery));
        classTypeButtons.add(new ClassTypeButton(x + 80, y, ClassType.SHAMAN, Texture.RELIK_FILTER_ICON, searchQuery));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        classTypeButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = false;

        for (ClassTypeButton classTypeButton : classTypeButtons) {
            if (classTypeButton.isMouseOver(mouseX, mouseY)) {
                clicked = classTypeButton.mouseClicked(mouseX, mouseY, button);
                break;
            }
        }

        guideScreen.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        List<StatProviderAndFilterPair> filterPairs = new ArrayList<>();

        for (ClassTypeButton classTypeButton : classTypeButtons) {
            StatProviderAndFilterPair filterPair = classTypeButton.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public void getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof ClassStatProvider)
                .map(statProvider -> (ClassStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get class stat provider"));
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        classTypeButtons.forEach(classTypeButton -> classTypeButton.updateStateFromQuery(searchQuery));
    }

    private static class ClassTypeButton extends GuideFilterButton<ClassStatProvider> {
        private final ClassType classType;

        protected ClassTypeButton(int x, int y, ClassType classType, Texture texture, ItemSearchQuery searchQuery) {
            super(x, y, texture);

            this.classType = classType;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            state = searchQuery.filters().values().stream()
                    .filter(filterPair -> filterPair.statProvider() instanceof ClassStatProvider)
                    .anyMatch(filterPair -> filterPair.statFilter().matches(EnumUtils.toNiceString(classType)));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(ClassStatProvider provider) {
            if (!state) return null;

            Optional<StringStatFilter> statFilterOpt =
                    new StringStatFilter.StringStatFilterFactory().create(classType.getName());

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }

        @Override
        protected String getFilterName() {
            return I18n.get("service.wynntils.itemFilter.stat.class.name") + " " + classType.getName();
        }
    }
}
