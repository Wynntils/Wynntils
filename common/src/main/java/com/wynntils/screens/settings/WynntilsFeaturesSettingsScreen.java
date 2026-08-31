/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Configurable;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.screens.base.widgets.WynntilsButton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WynntilsFeaturesSettingsScreen extends BaseWynntilsBookSettingsScreen {
    private WynntilsFeaturesSettingsScreen(Screen previousScreen) {
        super(previousScreen);
    }

    public static Screen create(Screen previousScreen) {
        return new WynntilsFeaturesSettingsScreen(previousScreen);
    }

    @Override
    protected int populateConfigurableEntries(
            List<WynntilsButton> configurables, List<Configurable> configurableList, int renderY) {
        Category oldCategory = null;

        for (Configurable configurable : configurableList) {
            Category category = getCategory(configurable);

            if (category != oldCategory) {
                addCategoryButton(configurables, renderY, category);
                oldCategory = category;
                renderY += 12;
            }

            addConfigurableButton(configurables, renderY, configurable);
            renderY += 12;

            if (configurable instanceof Feature feature) {
                renderY = addNestedConfigurableEntries(configurables, feature, renderY);
            }
        }

        return renderY;
    }

    private int addNestedConfigurableEntries(List<WynntilsButton> configurables, Feature feature, int renderY) {
        for (Feature subFeature : Managers.Feature.getSubFeatures(feature).stream()
                .filter(subFeature -> isSubFeatureFiltered(feature, subFeature))
                .filter(subFeature -> shouldShowNestedConfigurable(feature, subFeature))
                .sorted()
                .toList()) {
            addConfigurableButton(configurables, renderY, subFeature);
            renderY += 12;

            for (Overlay subOverlay : Managers.Overlay.getFeatureOverlays(subFeature).stream()
                    .filter(this::isOverlayFiltered)
                    .filter(subOverlay -> shouldShowNestedConfigurable(feature, subOverlay))
                    .sorted()
                    .toList()) {
                addConfigurableButton(configurables, renderY, subOverlay);
                renderY += 12;
            }
        }

        for (Overlay overlay : Managers.Overlay.getFeatureOverlays(feature).stream()
                .filter(this::isOverlayFiltered)
                .filter(overlay -> shouldShowNestedConfigurable(feature, overlay))
                .sorted()
                .toList()) {
            addConfigurableButton(configurables, renderY, overlay);
            renderY += 12;
        }

        return renderY;
    }

    @Override
    protected List<Configurable> getCurrentRootConfigurables() {
        return Managers.Feature.getFeatures().stream()
                .map(feature -> (Configurable) feature)
                .toList();
    }

    @Override
    protected List<Configurable> getFilteredConfigurables(Category selectedCategory) {
        List<Configurable> filteredConfigurables = new ArrayList<>();
        boolean hasSearch = !getSearchInput().isEmpty();

        if (selectedCategory != null) {
            filteredConfigurables.addAll(Managers.Feature.getFeatures().stream()
                    .filter(feature -> !Managers.Feature.isSubFeature(feature))
                    .filter(this::isFeatureFiltered)
                    .filter(feature -> isCategoryMatchingTree(feature, selectedCategory))
                    .sorted()
                    .toList());

            if (hasSearch) {
                filteredConfigurables.addAll(Managers.Feature.getFeatures().stream()
                        .filter(feature -> !Managers.Feature.isSubFeature(feature))
                        .filter(this::isFeatureFiltered)
                        .filter(feature -> !isCategoryMatchingTree(feature, selectedCategory))
                        .sorted()
                        .toList());
            }
        } else {
            filteredConfigurables.addAll(Managers.Feature.getFeatures().stream()
                    .filter(feature -> !Managers.Feature.isSubFeature(feature))
                    .filter(this::isFeatureFiltered)
                    .sorted()
                    .toList());
        }

        return filteredConfigurables;
    }

    @Override
    protected List<Configurable> getSelectableConfigurables() {
        return Stream.concat(
                        Managers.Feature.getFeatures().stream(),
                        Managers.Feature.getFeatures().stream()
                                .map(Managers.Overlay::getFeatureOverlays)
                                .flatMap(Collection::stream)
                                .map(overlay -> (Configurable) overlay))
                .toList();
    }

    @Override
    protected List<Configurable> getConfigurableMapConfigurables() {
        return Stream.concat(
                        getCurrentRootConfigurables().stream(),
                        Managers.Overlay.getOverlays().stream().map(overlay -> (Configurable) overlay))
                .toList();
    }

    private boolean isCategoryMatching(Configurable configurable, Category selectedCategory) {
        return getCategory(configurable) == selectedCategory;
    }

    private boolean isCategoryMatchingTree(Feature feature, Category selectedCategory) {
        return isCategoryMatching(feature, selectedCategory)
                || Managers.Overlay.getFeatureOverlays(feature).stream()
                        .anyMatch(overlay -> isCategoryMatching(overlay, selectedCategory))
                || Managers.Feature.getSubFeatures(feature).stream()
                        .anyMatch(subFeature -> isCategoryMatchingTree(subFeature, selectedCategory));
    }

    private boolean shouldShowNestedConfigurable(Feature parent, Configurable configurable) {
        if (getSelectedCategory() == null || !getSearchInput().isEmpty()) return true;

        return isCategoryMatching(parent, getSelectedCategory())
                || isCategoryMatching(configurable, getSelectedCategory());
    }

    private boolean isFeatureFiltered(Feature feature) {
        if (getSearchInput().isEmpty()) {
            return switch (getEnabledFilterType()) {
                case NEUTRAL -> true;
                case ENABLED -> feature.isEnabled();
                case DISABLED -> !feature.isEnabled();
            };
        }

        boolean featureSearchMatch = searchMatches(feature)
                || feature.getVisibleConfigOptions().stream().anyMatch(this::configOptionContains);

        boolean anyOverlayMatches =
                Managers.Overlay.getFeatureOverlays(feature).stream().anyMatch(this::overlaySearchMatches);

        boolean anySubFeatureMatches =
                Managers.Feature.getSubFeatures(feature).stream().anyMatch(this::isFeatureFiltered);

        return (featureSearchMatch || anyOverlayMatches || anySubFeatureMatches);
    }

    private boolean isSubFeatureFiltered(Feature parent, Feature subFeature) {
        if (!getSearchInput().isEmpty()) {
            return isFeatureFiltered(subFeature);
        }

        return switch (getEnabledFilterType()) {
            case NEUTRAL -> true;
            case ENABLED -> parent.isEnabled();
            case DISABLED -> !parent.isEnabled();
        };
    }

    private boolean isOverlayFiltered(Overlay overlay) {
        if (getSearchInput().isEmpty()) {
            Feature parent = Managers.Overlay.getOverlayParent(overlay);
            if (Managers.Feature.isSubFeature(parent)) {
                Feature grandparent = Managers.Feature.getParentFeature(parent);
                parent = grandparent;
            }

            boolean parentEnabled = parent != null && parent.isEnabled();

            return switch (getEnabledFilterType()) {
                case ENABLED -> parentEnabled;
                case DISABLED -> !parentEnabled;
                case NEUTRAL -> true;
            };
        }

        return overlaySearchMatches(overlay);
    }

    private Category getCategory(Configurable configurable) {
        if (configurable instanceof Feature feature) {
            return feature.getCategory();
        } else if (configurable instanceof Overlay overlay) {
            return Managers.Overlay.getOverlayParent(overlay).getCategory();
        } else {
            throw new IllegalStateException("Unknown configurable type: " + configurable.getClass());
        }
    }

    private boolean overlaySearchMatches(Overlay overlay) {
        return searchMatches(overlay)
                || overlay.getVisibleConfigOptions().stream().anyMatch(this::configOptionContains);
    }

    @Override
    protected boolean shouldShowCategoryControls() {
        return true;
    }

    @Override
    protected String getCategoryTitle(Category selectedCategory) {
        return selectedCategory == null
                ? I18n.get("screens.wynntils.settingsScreen.all")
                : I18n.get(selectedCategory.toString());
    }

    @Override
    protected Component getSettingsViewSwitchTargetName() {
        return Component.literal("Core");
    }

    @Override
    protected Screen createSettingsViewSwitchScreen(Screen previousScreen) {
        return WynntilsCoreSettingsScreen.create(previousScreen);
    }

    @Override
    protected boolean shouldIncludeInactiveOverlaysInConfigList() {
        return true;
    }
}
