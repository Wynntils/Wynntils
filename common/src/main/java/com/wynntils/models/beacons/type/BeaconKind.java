/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;

public interface BeaconKind {
    CustomColor getCustomColor();

    float getCustomModelData();

    default boolean matches(ItemStack itemStack) {
        if (itemStack.getItem() != Items.POTION) return false;
        if (getCustomModelData() == -1) return false;

        PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return false;

        CustomModelData potionCustomModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (potionCustomModelData == null) return false;

        Optional<Float> customModel = potionCustomModelData.floats().stream()
                .filter(value -> value.equals(getCustomModelData()))
                .findFirst();
        if (customModel.isEmpty()) return false;

        int potionCustomColor = potionContents.customColor().orElse(CommonColors.WHITE.asInt());

        return getCustomColor().equals(CustomColor.fromInt(potionCustomColor));
    }
}
