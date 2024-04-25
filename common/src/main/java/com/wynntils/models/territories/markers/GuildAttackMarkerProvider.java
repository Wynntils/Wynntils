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
import com.wynntils.services.map.pois.MarkerPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.stream.Stream;

public class GuildAttackMarkerProvider implements MarkerProvider<MarkerPoi> {
    @Override
    public Stream<MarkerInfo> getMarkerInfos() {
        List<TerritoryAttackTimer> attackTimers = Models.GuildAttackTimer.getAttackTimers();

        int lowestTimer = attackTimers.stream()
                .mapToInt(TerritoryAttackTimer::asSeconds)
                .min()
                .orElse(0);

        return attackTimers.stream().map(attackTimer -> {
            CustomColor beaconColor = attackTimer.defense().isEmpty()
                    ? CommonColors.WHITE
                    : CustomColor.fromChatFormatting(attackTimer.defense().get().getDefenceColor());

            return new MarkerInfo(
                    attackTimer.territoryProfile().getFriendlyName(),
                    () -> attackTimer.territoryProfile().getCenterLocation().asLocation(),
                    lowestTimer == attackTimer.asSeconds() ? Texture.STAR : Texture.WALL,
                    beaconColor,
                    CommonColors.WHITE,
                    beaconColor);
        });
    }

    @Override
    public Stream<MarkerPoi> getPois() {
        List<TerritoryAttackTimer> attackTimers = Models.GuildAttackTimer.getAttackTimers();

        int lowestTimer = attackTimers.stream()
                .mapToInt(TerritoryAttackTimer::asSeconds)
                .min()
                .orElse(0);

        return attackTimers.stream()
                .map(attackTimer -> new MarkerPoi(
                        attackTimer.territoryProfile().getCenterLocation(),
                        attackTimer.territoryProfile().getFriendlyName(),
                        lowestTimer == attackTimer.asSeconds() ? Texture.STAR : Texture.WALL));
    }

    @Override
    public boolean isEnabled() {
        TerritoryAttackTimerOverlayFeature feature =
                Managers.Feature.getFeatureInstance(TerritoryAttackTimerOverlayFeature.class);
        return feature.isEnabled() && feature.displayBeaconBeam.get();
    }
}