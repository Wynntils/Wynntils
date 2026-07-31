/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

import com.wynntils.models.items.encoding.type.ItemType;
import com.wynntils.models.rewards.type.TomeType;

public record SavableTome(TomeType type, String itemName, String encoded, ItemType itemType) implements SavableItem {
    public SavableTome(TomeType type, String itemName, String encoded) {
        this(type, itemName, encoded, ItemType.TOME);
    }
}
