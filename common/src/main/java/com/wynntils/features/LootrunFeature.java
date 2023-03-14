/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;

@StartDisabled
public class LootrunFeature extends Feature {
    public static LootrunFeature INSTANCE;

    @Config
    public PathType pathType = PathType.TEXTURED;

    @Config
    public CustomColor activePathColor = CommonColors.LIGHT_BLUE;

    @Config
    public CustomColor recordingPathColor = CommonColors.RED;

    @Config
    public boolean rainbowLootRun = false;

    @Config
    public int cycleDistance = 20; // TODO limit this later

    @Config
    public boolean showNotes = true;

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        Models.Lootrun.recompileLootrun(false);
    }

    public enum PathType {
        TEXTURED,
        LINE
    }
}
