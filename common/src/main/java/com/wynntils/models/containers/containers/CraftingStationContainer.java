/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import com.wynntils.models.profession.type.ProfessionType;
import java.util.regex.Pattern;

public class CraftingStationContainer extends Container implements HighlightableProfessionProperty {
    private final ProfessionType professionType;

    public CraftingStationContainer(Pattern titlePattern, ProfessionType professionType) {
        super(titlePattern);

        this.professionType = professionType;
    }

    @Override
    public ContainerBounds getBounds() {
        // Includes both the crafting station and the player inventory
        return new ContainerBounds(0, 0, 6, 8);
    }

    public ProfessionType getProfessionType() {
        return professionType;
    }

    @Override
    public String getContainerName() {
        return professionType.getDisplayName() + "Station";
    }
}
