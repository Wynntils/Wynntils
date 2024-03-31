/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

public interface RequirementItemProperty {
    /**
     * @return true if the item's lore has no X's in it, for the requirements section
     */
    boolean meetsActualRequirements();
}
