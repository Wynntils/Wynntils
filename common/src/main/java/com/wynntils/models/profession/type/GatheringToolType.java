/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

public enum GatheringToolType {
    AXE(ProfessionType.WOODCUTTING, "axe", "\uE010"),
    PICKAXE(ProfessionType.MINING, "pickaxe", "\uE012"),
    FISHING_ROD(ProfessionType.FISHING, "rod", "\uE013"),
    SCYTHE(ProfessionType.FARMING, "scythe", "\uE011");

    private final ProfessionType professionType;
    private final String apiName;
    private final String emblemCharacter;

    GatheringToolType(ProfessionType professionType, String apiName, String emblemCharacter) {
        this.professionType = professionType;
        this.apiName = apiName;
        this.emblemCharacter = emblemCharacter;
    }

    public static GatheringToolType fromApiName(String apiName) {
        for (GatheringToolType toolType : values()) {
            if (toolType.apiName.equals(apiName)) {
                return toolType;
            }
        }

        return null;
    }

    public ProfessionType getProfessionType() {
        return professionType;
    }

    public String getEmblemCharacter() {
        return emblemCharacter;
    }
}
