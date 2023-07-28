/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import java.util.Locale;

public record ToolProfile(ToolProfile.ToolType toolType, int tier) {
    public static ToolProfile fromString(String toolTypeName, int tier) {
        ToolType toolType = ToolType.fromString(toolTypeName);
        if (toolType == null) return null;

        return new ToolProfile(toolType, tier);
    }

    public int getLevel() {
        return tier == 1 ? 1 : 5 + (tier - 2) * 10;
    }

    @Override
    public String toString() {
        return "ToolProfile{" + "toolType=" + toolType + ", tier=" + tier + '}';
    }

    public enum ToolType {
        PICKAXE(ProfessionType.MINING),
        AXE(ProfessionType.WOODCUTTING),
        SCYTHE(ProfessionType.FARMING),
        ROD(ProfessionType.FISHING);

        private final ProfessionType professionType;

        ToolType(ProfessionType professionType) {
            this.professionType = professionType;
        }

        private static ToolType fromString(String str) {
            try {
                return ToolType.valueOf(str.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
