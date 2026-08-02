/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

import com.wynntils.models.items.encoding.type.ItemType;

public record SavableGear(String encoded, String itemName, ItemType itemType) implements SavableItem {
    public SavableGear() {
        this("", null, ItemType.GEAR);
    }

    public SavableGear(String encoded, String itemName) {
        this(encoded, itemName, ItemType.GEAR);
    }
}
