/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;

@FeatureInfo(
        stability = Stability.STABLE,
        gameplay = GameplayImpact.MEDIUM,
        performance = PerformanceImpact.MEDIUM)
public class HealthPotionBlockerFeature extends Feature {
    // TODO
}
