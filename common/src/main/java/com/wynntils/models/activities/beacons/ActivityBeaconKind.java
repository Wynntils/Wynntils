/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.beacons;

import com.wynntils.core.components.Models;
import com.wynntils.models.beacons.type.BeaconKind;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;

public enum ActivityBeaconKind implements BeaconKind {
    QUEST(Models.Activity.BEACON_COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0x29CC96)),
    STORYLINE_QUEST(Models.Activity.BEACON_COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0x33B33B)),
    MINI_QUEST(Models.Activity.BEACON_COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xB38FAD)),
    WORLD_EVENT(Models.Activity.BEACON_COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0x00BDBF)),
    DISCOVERY(Models.Activity.BEACON_COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xA1C3E6)),
    CAVE(Models.Activity.BEACON_COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xFF8C19)),
    DUNGEON(Models.Activity.BEACON_COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xCC6677)),
    RAID(Models.Activity.BEACON_COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xD6401E)),
    BOSS_ALTAR(Models.Activity.BEACON_COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0xF2D349)),
    LOOTRUN_CAMP(Models.Activity.BEACON_COLOR_CUSTOM_MODEL_DATA, CustomColor.fromInt(0x3399CC));

    private final float customModelData;
    private final CustomColor customColor;

    ActivityBeaconKind(float customModelData, CustomColor customColor) {
        this.customModelData = customModelData;
        this.customColor = customColor;
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        if (itemStack.getItem() != Items.POTION) return false;

        PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return false;

        CustomModelData potionCustomModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (potionCustomModelData == null) return false;

        Optional<Float> customModel = potionCustomModelData.floats().stream()
                .filter(value -> value.equals(customModelData))
                .findFirst();
        if (customModel.isEmpty()) return false;

        int potionCustomColor = potionContents.customColor().orElse(CommonColors.WHITE.asInt());

        return this.customColor.equals(CustomColor.fromInt(potionCustomColor));
    }
}
