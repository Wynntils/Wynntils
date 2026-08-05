/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.user;

import com.wynntils.services.mapdata.attributes.resolving.OverrideMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.type.AbstractMapDataOverrideProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class UserOverrideProvider extends AbstractMapDataOverrideProvider {
    private List<UserOverrideProviderInfo> infos = new ArrayList<>();

    public void updateInfos(List<UserOverrideProviderInfo> newInfos) {
        this.infos = new ArrayList<>(newInfos);
        callbacks.forEach(c -> c.accept(null));
    }

    public String getProviderId() {
        return "user-override";
    }

    @Override
    public MapAttributes getOverrideAttributes(MapFeature mapFeature) {
        // Feature overrides
        Stream<MapAttributes> featureAttrs = infos.stream()
                .filter(c -> c.targetFeatureId() != null)
                .filter(c -> c.overridesFeature(mapFeature.getCategoryId(), mapFeature.getFeatureId()))
                .map(UserOverrideProviderInfo::attributes);

        // Category overrides
        Stream<MapAttributes> categoryAttrs = infos.stream()
                .filter(c -> c.targetFeatureId() == null)
                .filter(c -> c.overridesFeature(mapFeature.getCategoryId(), mapFeature.getFeatureId()))
                .sorted(Comparator.comparingInt((UserOverrideProviderInfo c) ->
                                c.targetCategoryId().length())
                        .reversed())
                .map(UserOverrideProviderInfo::attributes);

        List<MapAttributes> attrs = Stream.concat(featureAttrs, categoryAttrs).toList();
        return OverrideMapAttributes.from(attrs).orElse(null);
    }

    @Override
    public Stream<String> getOverridenFeatureIds() {
        return infos.stream().map(UserOverrideProviderInfo::targetFeatureId).filter(Objects::nonNull);
    }

    @Override
    public Stream<String> getOverridenCategoryIds() {
        return infos.stream().map(UserOverrideProviderInfo::targetCategoryId).filter(Objects::nonNull);
    }
}
