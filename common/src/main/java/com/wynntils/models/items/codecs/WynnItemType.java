/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.wynntils.models.items.ItemModel;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import net.minecraft.util.StringRepresentable;

public enum WynnItemType implements StringRepresentable {
    // TODO Add all types
    GEAR("gear"),
    FALLBACK("fallback");

    public static final Codec<WynnItemType> CODEC = StringRepresentable.fromEnum(WynnItemType::values);

    private final String serializedName;

    WynnItemType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public MapCodec<? extends WynnItem> getCodec() {
        return switch (this) {
            case GEAR -> GearItem.CODEC;
            case FALLBACK -> ItemModel.FallbackItem.CODEC;
        };
    }
}
