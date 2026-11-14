/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.handlers.tooltip.type.ItemEmblemSprite;

public enum GatheringToolType {
    AXE(ProfessionType.WOODCUTTING, "axe", ItemEmblemSprite.AXE),
    PICKAXE(ProfessionType.MINING, "pickaxe", ItemEmblemSprite.PICKAXE),
    FISHING_ROD(ProfessionType.FISHING, "rod", ItemEmblemSprite.FISHING_ROD),
    SCYTHE(ProfessionType.FARMING, "scythe", ItemEmblemSprite.SCYTHE);

    private final ProfessionType professionType;
    private final String apiName;
    private final ItemEmblemSprite emblemSprite;

    GatheringToolType(ProfessionType professionType, String apiName, ItemEmblemSprite emblemSprite) {
        this.professionType = professionType;
        this.apiName = apiName;
        this.emblemSprite = emblemSprite;
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

    public ItemEmblemSprite getEmblemSprite() {
        return emblemSprite;
    }
}
