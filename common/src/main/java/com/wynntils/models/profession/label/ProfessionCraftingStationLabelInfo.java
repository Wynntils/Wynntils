/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class ProfessionCraftingStationLabelInfo extends ProfessionLabelInfo {
    public ProfessionCraftingStationLabelInfo(
            StyledText label, String name, Location location, Entity entity, ProfessionType professionType) {
        super(label, name, location, entity, professionType);
    }

    @Override
    public String toString() {
        return "ProfessionCraftingStationLabelInfo{" + "professionType="
                + professionType + ", label="
                + label + ", name='"
                + name + '\'' + ", location="
                + location + ", entity="
                + entity + '}';
    }
}
