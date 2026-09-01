/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.models.gear.type.GearInstanceRequirements;

public interface RequirementItemProperty {
    /**
     * @return true if the item is usable by the player
     */
    boolean meetsActualRequirements();

    /**
     * @return the met gear requirements info for the item
     */
    GearInstanceRequirements getGearInstanceRequirements();
}
