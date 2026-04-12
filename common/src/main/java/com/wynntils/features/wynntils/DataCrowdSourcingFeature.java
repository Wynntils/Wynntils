/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.crowdsource.type.CrowdSourcedDataType;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.utils.type.OptionalBoolean;
import java.util.Map;
import java.util.TreeMap;

@ConfigCategory(Category.WYNNTILS)
public class DataCrowdSourcingFeature extends Feature {
    @Persisted
    public final HiddenConfig<Map<CrowdSourcedDataType, OptionalBoolean>> crowdSourcedDataTypeEnabledMap =
            new HiddenConfig<>(new TreeMap<>());

    public DataCrowdSourcingFeature() {
        super(ProfileDefault.DISABLED);
    }
}
