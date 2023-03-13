/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Models;
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
    public PathType pathType = PathType.TEXTURED;

    @ConfigInfo
    public CustomColor activePathColor = CommonColors.LIGHT_BLUE;

    @ConfigInfo
    public CustomColor recordingPathColor = CommonColors.RED;

    @ConfigInfo
    public boolean rainbowLootRun = false;

    @ConfigInfo
    public int cycleDistance = 20; // TODO limit this later

    @ConfigInfo
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
