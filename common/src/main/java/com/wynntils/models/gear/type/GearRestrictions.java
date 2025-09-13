/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum GearRestrictions implements StringRepresentable {
    NONE(""),
    UNTRADABLE("Untradable Item"),
    QUEST_ITEM("Quest Item"),
    SOULBOUND("Soulbound Item");

    public static final Codec<GearRestrictions> CODEC = StringRepresentable.fromEnum(GearRestrictions::values);

    private final String description;

    GearRestrictions(String description) {
        this.description = description;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static GearRestrictions fromString(String typeStr) {
        for (GearRestrictions type : GearRestrictions.values()) {
            if (type.name().replaceAll("_", " ").toLowerCase(Locale.ROOT).equals(typeStr.toLowerCase(Locale.ROOT))) {
                return type;
            }
        }

        return null;
    }

    public String getDescription() {
        return description;
    }
}
