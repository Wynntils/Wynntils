/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum StatUnit implements StringRepresentable {
    RAW(""),
    PERCENT("%"),
    PER_3_S("/3s"),
    PER_5_S("/5s"),
    TIER(" tier");

    public static final Codec<StatUnit> CODEC = StringRepresentable.fromEnum(StatUnit::values);

    private final String displayName;

    StatUnit(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getDisplayName() {
        return displayName;
    }
}
