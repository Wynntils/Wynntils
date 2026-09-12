/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.mapdata;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.screens.territorymanagement.TerritoryManagementHolder;
import com.wynntils.screens.territorymanagement.TerritoryManagementScreen;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;

/**
 * Provides {@link ManageTerritoryArea} features for {@link TerritoryManagementScreen}'s map mode.
 * <p>
 * This exists purely for cache invalidation: {@code MapDataService#resolveMapAttributes} memoizes
 * resolved attributes per {@link MapFeature} instance, and the only way to evict a stale entry is
 * {@code notifyCallbacks} from a registered provider - a screen-local feature list has no way to do
 * that. The colors of a managed territory change on every container update (upgrades, selection,
 * connections) and on every info type switch, so this provider reuses {@link ManageTerritoryArea}
 * instances by territory name across updates and explicitly invalidates them instead of doing a full
 * rebuild-and-notify every time, the way {@code TerritoryProvider} does for the real territories.
 * <p>
 * {@link #getFeatures()} returns an empty stream unless the territory management screen is currently
 * open, since both {@code MainMapScreen} and {@code MinimapOverlay} consume the unfiltered
 * {@code Services.MapData.getFeatures()} and must never see these.
 */
public class ManageTerritoryProvider extends BuiltInProvider {
    private static final Map<String, ManageTerritoryArea> FEATURES_BY_NAME = new LinkedHashMap<>();

    private static ManageTerritoryProvider instance;

    public ManageTerritoryProvider() {
        instance = this;
    }

    /**
     * Rebuilds this provider's features from the holder's current territory items, reusing
     * existing {@link ManageTerritoryArea} instances by territory name where possible, and
     * invalidating the resolved-attribute cache for every territory whose backing item was
     * refreshed.
     */
    public static void updateFeatures(TerritoryManagementHolder holder) {
        if (instance == null) return;

        Set<String> currentNames = new HashSet<>();

        for (Pair<ItemStack, TerritoryItem> territoryItemPair : holder.territoryItems()) {
            TerritoryItem territoryItem = territoryItemPair.b();
            String name = territoryItem.getName();
            currentNames.add(name);

            ManageTerritoryArea existing = FEATURES_BY_NAME.get(name);
            if (existing != null) {
                existing.setTerritoryItemSupplier(() -> territoryItem);
                instance.notifyCallbacks(existing);
            } else {
                TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfile(name);
                if (territoryProfile == null) continue;

                FEATURES_BY_NAME.put(name, new ManageTerritoryArea(holder, territoryProfile, () -> territoryItem));
            }
        }

        FEATURES_BY_NAME.keySet().removeIf(name -> {
            if (currentNames.contains(name)) return false;

            instance.notifyCallbacks(FEATURES_BY_NAME.get(name));
            return true;
        });
    }

    /**
     * Forces every currently-provided feature's resolved attributes to be recomputed, without
     * changing which territories are provided. Used when the info type (which colors are shown)
     * is switched.
     */
    public static void refreshColors() {
        if (instance == null) return;

        FEATURES_BY_NAME.values().forEach(instance::notifyCallbacks);
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        if (!(McUtils.screen() instanceof TerritoryManagementScreen)) {
            return Stream.empty();
        }

        return FEATURES_BY_NAME.values().stream().map(area -> (MapFeature) area);
    }

    @Override
    public String getProviderId() {
        return "manage-territory";
    }

    @Override
    public void reloadData() {
        FEATURES_BY_NAME.values().forEach(this::notifyCallbacks);
        FEATURES_BY_NAME.clear();
    }
}
