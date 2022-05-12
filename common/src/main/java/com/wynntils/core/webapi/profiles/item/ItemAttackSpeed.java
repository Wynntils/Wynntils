/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles.item;

import com.google.gson.annotations.SerializedName;
import net.minecraft.ChatFormatting;

public enum ItemAttackSpeed {
    @SerializedName("SUPER_FAST")
    SuperFast("Super Fast Attack Speed", 3),
    @SerializedName("VERY_FAST")
    VeryFast("Very Fast Attack Speed", 2),
    @SerializedName("FAST")
    Fast("Fast Attack Speed", 1),
    @SerializedName("NORMAL")
    Normal("Normal Attack Speed", 0),
    @SerializedName("SLOW")
    Slow("Slow Attack Speed", -1),
    @SerializedName("VERY_SLOW")
    VerySlow("Very Slow Attack Speed", -2),
    @SerializedName("SUPER_SLOW")
    SuperSlow("Super Slow Attack Speed", -3);

    final String name;
    final int offset;

    ItemAttackSpeed(String name, int offset) {
        this.name = name;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    public String asLore() {
        return ChatFormatting.GRAY + name;
    }
}
