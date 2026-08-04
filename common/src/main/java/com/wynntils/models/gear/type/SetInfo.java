/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;

public record SetInfo(String name, String cleanName, Map<Integer, SetBonus> bonuses, List<String> items) {
    /**
     * @param numberOfItems The number of items equipped to get the set bonus for
     *                      (clamped to the number of items in the set)
     * @return A map of stat names to the bonus value for that stat
     */
    public SetBonus getBonusForItems(int numberOfItems) {
        return bonuses.get(Mth.clamp(numberOfItems, 1, items.size()));
    }
}
