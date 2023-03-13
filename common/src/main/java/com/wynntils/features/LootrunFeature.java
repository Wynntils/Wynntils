/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.ConfigInfo;
import com.wynntils.core.features.StateManagedFeature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;

@StartDisabled
public class LootrunFeature extends StateManagedFeature {
    public static LootrunFeature INSTANCE;

    @ConfigInfo
    public Config<PathType> pathType = new Config<>(PathType.TEXTURED);

    @ConfigInfo
    public Config<CustomColor> activePathColor = new Config<>(CommonColors.LIGHT_BLUE);

    @ConfigInfo
    public Config<CustomColor> recordingPathColor = new Config<>(CommonColors.RED);

    @ConfigInfo
    public Config<Boolean> rainbowLootRun = new Config<>(false);

    @ConfigInfo
    public Config<Integer> cycleDistance = new Config<>(20); // TODO limit this later

    @ConfigInfo
    public Config<Boolean> showNotes = new Config<>(true);

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        Models.Lootrun.recompileLootrun(false);
    }

    public enum PathType {
        TEXTURED,
        LINE
    }
}
