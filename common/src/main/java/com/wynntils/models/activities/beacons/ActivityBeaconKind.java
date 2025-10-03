/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.beacons;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.models.beacons.type.BeaconKind;
import com.wynntils.utils.colors.CustomColor;

public enum ActivityBeaconKind implements BeaconKind {
    QUEST(CustomColor.fromInt(0x29CC96)),
    STORYLINE_QUEST(CustomColor.fromInt(0x33B33B)),
    MINI_QUEST(CustomColor.fromInt(0xB38FAD)),
    WORLD_EVENT(CustomColor.fromInt(0x00BDBF)),
    DISCOVERY(CustomColor.fromInt(0xA1C3E6)),
    CAVE(CustomColor.fromInt(0xFF8C19)),
    DUNGEON(CustomColor.fromInt(0xCC6677)),
    RAID(CustomColor.fromInt(0xD6401E)),
    BOSS_ALTAR(CustomColor.fromInt(0xF2D349)),
    LOOTRUN_CAMP(CustomColor.fromInt(0x3399CC));

    private final CustomColor customColor;

    ActivityBeaconKind(CustomColor customColor) {
        this.customColor = customColor;
    }

    @Override
    public CustomColor getCustomColor() {
        return customColor;
    }

    @Override
    public float getCustomModelData() {
        return Services.CustomModel.getFloat(Models.Beacon.BEACON_COLOR_CUSTOM_MODEL_DATA_KEY)
                .orElse(-1f);
    }
}
