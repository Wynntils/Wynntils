/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.utils.EnumUtils;

public enum MiscGatheringType {
    LARBONIC_GEODE(ProfessionType.MINING, 105),
    RED_ALDER(ProfessionType.WOODCUTTING, 10),
    BAMBOO(ProfessionType.WOODCUTTING, 80),
    CEMBRA_PINE(ProfessionType.WOODCUTTING, 80),
    DOUGLAS_FIR(ProfessionType.WOODCUTTING, 80),
    FLERISI_TREE(ProfessionType.WOODCUTTING, 85),
    FLERISI_TRUNK(ProfessionType.WOODCUTTING, 85),
    BLOSSOM(ProfessionType.WOODCUTTING, 100),
    INDUSTREE(ProfessionType.WOODCUTTING, 133),
    VOIDGLOOM(ProfessionType.FARMING, 105),
    ABYSSAL_MATTER(ProfessionType.FISHING, 90);

    private final ProfessionType professionType;
    private final int level;

    MiscGatheringType(ProfessionType professionType, int level) {
        this.professionType = professionType;
        this.level = level;
    }

    public static MiscGatheringType fromResourceName(String resourceName) {
        for (MiscGatheringType gatheringType : MiscGatheringType.values()) {
            if (EnumUtils.toNiceString(gatheringType).equalsIgnoreCase(resourceName)) {
                return gatheringType;
            }
        }

        return null;
    }

    public ProfessionType getProfessionType() {
        return professionType;
    }

    public int getLevel() {
        return level;
    }
}
