/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.type.Location;

public class ProfessionCraftingStationLabelInfo extends ProfessionLabelInfo {
    public ProfessionCraftingStationLabelInfo(
            StyledText label, String name, Location location, ProfessionType professionType) {
        super(label, name, location, professionType);
    }

    @Override
    public String toString() {
        return "GatheringStationLabelInfo{" + "professionType="
                + professionType + ", label="
                + label + ", labelString='"
                + name + '\'' + ", location="
                + location + '}';
    }
}
