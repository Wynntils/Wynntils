/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories.providers;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.TerritoryAttackTimer;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.services.mapdata.attributes.MapMarkerOptionsBuilder;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.builtin.MapIconsProvider;
import com.wynntils.utils.MapDataUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.Pair;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuildAttackLocationProvider extends BuiltInProvider {
    @Override
    public Stream<MapFeature> getFeatures() {
        Map<String, TerritoryAttackTimer> attackedTerritories = Models.GuildAttackTimer.getUpcomingTimers()
                .collect(Collectors.toMap(TerritoryAttackTimer::territoryName, timer -> timer));

        return Models.Territory.getTerritoryProfiles().stream()
                .map(profile -> {
                    TerritoryAttackTimer attackTimer = attackedTerritories.get(profile.getName());
                    return Pair.of(profile, attackTimer);
                })
                .filter(pair -> Objects.nonNull(pair.b()))
                .map(pair -> new TerritoryAttackLocation(pair.a(), pair.b()));
    }

    @Override
    public String getProviderId() {
        return "guild-attacks";
    }

    @Override
    public void reloadData() {
        // This built-in provider does not load any external data, so no explicit reload is needed.
    }

    public static class TerritoryAttackLocation extends MapLocationImpl {
        private final TerritoryProfile territoryProfile;
        private final TerritoryAttackTimer attackTimer;

        public TerritoryAttackLocation(TerritoryProfile territoryProfile, TerritoryAttackTimer attackTimer) {
            super(
                    MapDataUtils.sanitizeFeatureId("territory-attack-" + territoryProfile.getName()),
                    "wynntils:territory:attack",
                    null,
                    territoryProfile.getCenterLocation().asLocation());
            this.territoryProfile = territoryProfile;
            this.attackTimer = attackTimer;
        }

        private Optional<CustomColor> getAttackColor() {
            CustomColor beaconColor = attackTimer.defense().isEmpty()
                    ? CommonColors.WHITE
                    : CustomColor.fromChatFormatting(attackTimer.defense().get().getDefenceColor());

            return Optional.of(beaconColor);
        }

        @Override
        public Optional<MapLocationAttributes> getAttributes() {
            return Optional.of(new AbstractMapLocationAttributes() {
                @Override
                public Optional<String> getLabel() {
                    return Optional.of(attackTimer.asString());
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return getAttackColor();
                }

                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    Optional<CustomColor> beaconColor = getAttackColor();
                    if (beaconColor.isEmpty()) return Optional.empty();

                    return Optional.of(new MapMarkerOptionsBuilder().withBeaconColor(beaconColor.get()));
                }

                @Override
                public Optional<String> getIconId() {
                    String minAttackTimerTerritory = Models.GuildAttackTimer.getUpcomingTimers()
                            .min(Comparator.comparing(TerritoryAttackTimer::territoryName))
                            .map(TerritoryAttackTimer::territoryName)
                            .orElse(null);
                    return Optional.of(
                            territoryProfile.getName().equals(minAttackTimerTerritory)
                                    ? MapIconsProvider.getIconIdFromTexture(Texture.STAR)
                                    : MapIconsProvider.getIconIdFromTexture(Texture.WALL));
                }
            });
        }
    }
}
