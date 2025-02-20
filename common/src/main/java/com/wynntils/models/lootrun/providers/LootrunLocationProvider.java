/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.providers;

import com.google.common.base.CaseFormat;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.combat.CustomLootrunBeaconsFeature;
import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.services.mapdata.attributes.MapMarkerOptionsBuilder;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.builtin.MapIconsProvider;
import com.wynntils.utils.MapDataUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CustomColor;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class LootrunLocationProvider extends BuiltInProvider {
    @Override
    public String getProviderId() {
        return "lootrun-locations";
    }

    @Override
    public void reloadData() {
        // This built-in provider does not load any external data, so no explicit reload is needed.
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        // FIXME: Feature-Model dependency
        if (!Models.Lootrun.getState().isRunning()
                || !Managers.Feature.getFeatureInstance(CustomLootrunBeaconsFeature.class)
                        .isEnabled()) return Stream.empty();

        return Models.Lootrun.getBeacons().entrySet().stream()
                .map(entry -> new LootrunLocation(entry.getValue().taskLocation(), entry.getKey()));
    }

    public static class LootrunLocation extends MapLocationImpl {
        private final TaskLocation taskLocation;
        private final LootrunBeaconKind beaconKind;

        public LootrunLocation(TaskLocation taskLocation, LootrunBeaconKind beaconKind) {
            super(
                    MapDataUtils.sanitizeFeatureId("lootrun-location-" + taskLocation.name()),
                    "wynntils:lootrun:" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, beaconKind.name()),
                    null,
                    taskLocation.location());
            this.taskLocation = taskLocation;
            this.beaconKind = beaconKind;
        }

        @Override
        public Optional<MapLocationAttributes> getAttributes() {
            return Optional.of(new AbstractMapLocationAttributes() {
                @Override
                public Optional<String> getLabel() {
                    return Optional.of(taskLocation.name() + " ("
                            + StringUtils.capitalizeFirst(beaconKind.name().toLowerCase(Locale.ROOT)) + ")");
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(beaconKind.getDisplayColor());
                }

                @Override
                public Optional<String> getIconId() {
                    return Optional.of(MapIconsProvider.getIconIdFromTexture(
                            taskLocation.taskType().getTexture()));
                }

                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    return Optional.of(new MapMarkerOptionsBuilder().withBeaconColor(beaconKind.getDisplayColor()));
                }
            });
        }
    }
}
