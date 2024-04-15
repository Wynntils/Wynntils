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
import com.wynntils.services.map.pois.MarkerPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import java.util.stream.Stream;

public class GuildAttackMarkerProvider implements MarkerProvider<MarkerPoi> {
    @Override
    public Stream<MarkerInfo> getMarkerInfos() {
        return Models.GuildAttackTimer.getAttackTimers().stream()
                .filter(attackTimer -> attackTimer.territoryProfile().isPresent())
                .map(attackTimer -> {
                    CustomColor beaconColor = attackTimer.defense() == null
                            ? CommonColors.WHITE
                            : CustomColor.fromChatFormatting(
                                    attackTimer.defense().getDefenceColor());

                    return new MarkerInfo(
                            attackTimer.territory(),
                            () -> attackTimer
                                    .territoryProfile()
                                    .get()
                                    .getCenterLocation()
                                    .asLocation(),
                            Texture.WALL,
                            beaconColor,
                            beaconColor,
                            CommonColors.WHITE);
                });
    }

    @Override
    public Stream<MarkerPoi> getPois() {
        return Models.GuildAttackTimer.getAttackTimers().stream()
                .filter(attackTimer -> attackTimer.territoryProfile().isPresent())
                .map(attackTimer -> new MarkerPoi(
                        attackTimer.territoryProfile().get().getCenterLocation(),
                        attackTimer.territory(),
                        Texture.WALL));
    }

    @Override
    public boolean isEnabled() {
        TerritoryAttackTimerOverlayFeature feature =
                Managers.Feature.getFeatureInstance(TerritoryAttackTimerOverlayFeature.class);
        return feature.isEnabled() && feature.displayBeaconBeam.get();
    }
}
