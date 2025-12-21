/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;

public class LootrunFeature extends Feature {
    @Persisted
    public final Config<PathType> pathType = new Config<>(PathType.TEXTURED);

    @Persisted
    public final Config<CustomColor> activePathColor = new Config<>(CommonColors.LIGHT_BLUE);

    @Persisted
    public final Config<CustomColor> recordingPathColor = new Config<>(CommonColors.RED);

    @Persisted
    public final Config<Boolean> rainbowLootRun = new Config<>(false);

    @Persisted
    public final Config<Integer> cycleDistance = new Config<>(20); // TODO limit this later

    @Persisted
    public final Config<Boolean> showNotes = new Config<>(true);

    public LootrunFeature() {
        // Enabled status for this doesn't really matter, but just for settings filters we disable it
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Services.LootrunPaths.recompileLootrun(false);
    }

    public enum PathType {
        TEXTURED,
        LINE
    }
}
