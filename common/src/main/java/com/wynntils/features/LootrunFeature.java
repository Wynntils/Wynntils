/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Services;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;

public class LootrunFeature extends Feature {
    @RegisterConfig
    public final Config<PathType> pathType = new Config<>(PathType.TEXTURED);

    @RegisterConfig
    public final Config<CustomColor> activePathColor = new Config<>(CommonColors.LIGHT_BLUE);

    @RegisterConfig
    public final Config<CustomColor> recordingPathColor = new Config<>(CommonColors.RED);

    @RegisterConfig
    public final Config<Boolean> rainbowLootRun = new Config<>(false);

    @RegisterConfig
    public final Config<Integer> cycleDistance = new Config<>(20); // TODO limit this later

    @RegisterConfig
    public final Config<Boolean> showNotes = new Config<>(true);

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        Services.LootrunPaths.recompileLootrun(false);
    }

    public enum PathType {
        TEXTURED,
        LINE
    }
}
