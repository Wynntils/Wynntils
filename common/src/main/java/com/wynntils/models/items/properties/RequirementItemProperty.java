/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

public interface RequirementItemProperty {
    /**
     * @return true if the item is usable by the player
     */
    boolean meetsActualRequirements();
}
