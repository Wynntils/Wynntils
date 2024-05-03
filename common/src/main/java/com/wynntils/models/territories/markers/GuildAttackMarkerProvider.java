/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories.markers;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.overlays.TerritoryAttackTimerOverlayFeature;
import com.wynntils.models.marker.type.MarkerInfo;
import com.wynntils.models.marker.type.MarkerProvider;
import com.wynntils.models.territories.TerritoryAttackTimer;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.services.map.pois.MarkerPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class GuildAttackMarkerProvider implements MarkerProvider<MarkerPoi> {
    @Override
    public Stream<MarkerInfo> getMarkerInfos() {
        List<TerritoryAttackTimer> attackTimers = Models.GuildAttackTimer.getAttackTimers();

        int lowestTimer = attackTimers.stream()
                .mapToInt(TerritoryAttackTimer::asSeconds)
                .min()
                .orElse(0);

        return attackTimers.stream()
                .map(attackTimer -> {
                    CustomColor beaconColor = attackTimer.defense().isEmpty()
                            ? CommonColors.WHITE
                            : CustomColor.fromChatFormatting(
                                    attackTimer.defense().get().getDefenceColor());

                    TerritoryProfile territoryProfile =
                            Models.Territory.getTerritoryProfile(attackTimer.territoryName());

                    if (territoryProfile == null) {
                        return null;
                    }

                    return new MarkerInfo(
                            attackTimer.territoryName(),
                            () -> territoryProfile.getCenterLocation().asLocation(),
                            lowestTimer == attackTimer.asSeconds() ? Texture.STAR : Texture.WALL,
                            beaconColor,
                            CommonColors.WHITE,
                            beaconColor);
                })
                .filter(Objects::nonNull);
    }

    @Override
    public Stream<MarkerPoi> getPois() {
        List<TerritoryAttackTimer> attackTimers = Models.GuildAttackTimer.getAttackTimers();

        int lowestTimer = attackTimers.stream()
                .mapToInt(TerritoryAttackTimer::asSeconds)
                .min()
                .orElse(0);

        return attackTimers.stream()
                .map(attackTimer -> {
                    TerritoryProfile territoryProfile =
                            Models.Territory.getTerritoryProfile(attackTimer.territoryName());

                    if (territoryProfile == null) {
                        return null;
                    }

                    return new MarkerPoi(
                            territoryProfile.getCenterLocation(),
                            territoryProfile.getFriendlyName(),
                            lowestTimer == attackTimer.asSeconds() ? Texture.STAR : Texture.WALL);
                })
                .filter(Objects::nonNull);
    }

    @Override
    public boolean isEnabled() {
        TerritoryAttackTimerOverlayFeature feature =
                Managers.Feature.getFeatureInstance(TerritoryAttackTimerOverlayFeature.class);
        return feature.isEnabled()
                && feature.displayBeaconBeam.get()
                && !Models.Lootrun.getState().isRunning();
    }
}
