/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.user;

import com.wynntils.services.mapdata.attributes.impl.MapAttributesImpl;

public record UserOverrideProviderInfo(
        String targetCategoryId, // null if overriding a feature
        String targetFeatureId, // null if overriding a category
        MapAttributesImpl attributes) {
    public UserOverrideProviderInfo {
        if (targetCategoryId == null && targetFeatureId == null) {
            throw new IllegalArgumentException("At least one target must be set");
        }
    }

    public boolean overridesFeature(String categoryId, String featureId) {
        if (targetFeatureId != null) {
            return targetFeatureId.equals(featureId);
        }

        return targetCategoryId != null && categoryId.startsWith(targetCategoryId);
    }
}
