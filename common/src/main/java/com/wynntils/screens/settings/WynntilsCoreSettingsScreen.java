/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.ConfigurableCoreComponent;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Service;
import com.wynntils.core.consumers.features.Configurable;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.screens.base.widgets.WynntilsButton;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WynntilsCoreSettingsScreen extends BaseWynntilsBookSettingsScreen {
    private WynntilsCoreSettingsScreen(Screen previousScreen) {
        super(previousScreen);
    }

    public static Screen create(Screen previousScreen) {
        return new WynntilsCoreSettingsScreen(previousScreen);
    }

    @Override
    protected int populateConfigurableEntries(
            List<WynntilsButton> configurables, List<Configurable> configurableList, int renderY) {
        String oldCoreType = null;

        for (Configurable configurable : configurableList) {
            ConfigurableCoreComponent coreComponent = (ConfigurableCoreComponent) configurable;
            String coreType = coreComponent.getTypeName();

            if (!coreType.equals(oldCoreType)) {
                addComponentTypeButton(configurables, renderY, coreType);
                oldCoreType = coreType;
                renderY += 12;
            }

            addConfigurableButton(configurables, renderY, configurable);
            renderY += 12;
        }

        return renderY;
    }

    private List<Configurable> getCoreConfigurables() {
        return Stream.of(Model.class, Service.class)
                .flatMap(componentClass -> WynntilsMod.getComponents(componentClass).stream())
                .map(component -> (Configurable) component)
                .filter(configurable -> !configurable.getVisibleConfigOptions().isEmpty())
                .toList();
    }

    @Override
    protected List<Configurable> getCurrentRootConfigurables() {
        return getCoreConfigurables();
    }

    @Override
    protected List<Configurable> getFilteredConfigurables(Category selectedCategory) {
        return getCoreConfigurables().stream()
                .filter(this::isCoreFiltered)
                .sorted(Comparator.comparing((Configurable configurable) -> configurable.getTypeName())
                        .thenComparing(Configurable::getTranslatedName))
                .toList();
    }

    @Override
    protected List<Configurable> getSelectableConfigurables() {
        return getCoreConfigurables();
    }

    @Override
    protected List<Configurable> getConfigurableMapConfigurables() {
        return getCurrentRootConfigurables();
    }

    private boolean isCoreFiltered(Configurable configurable) {
        if (!(configurable instanceof ConfigurableCoreComponent coreComponent)) return false;

        if (getSearchInput().isEmpty()) {
            return true;
        }

        return searchMatches(coreComponent)
                || coreComponent.getVisibleConfigOptions().stream().anyMatch(this::configOptionContains);
    }

    @Override
    protected boolean shouldShowCategoryControls() {
        return false;
    }

    @Override
    protected String getCategoryTitle(Category selectedCategory) {
        return I18n.get("screens.wynntils.settingsScreen.core");
    }

    @Override
    protected Component getSettingsViewSwitchTargetName() {
        return Component.literal("Features");
    }

    @Override
    protected Screen createSettingsViewSwitchScreen(Screen previousScreen) {
        return WynntilsFeaturesSettingsScreen.create(previousScreen);
    }
}
