/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.statistics;

import net.minecraft.client.resources.language.I18n;

public enum StatisticKind {
    DAMAGE_DEALT("damage_dealt"),
    SPELLS_CAST("spells_cast");

    private final String id;

    StatisticKind(String id) {
        this.id = id;
    }

    public static StatisticKind from(String statisticId) {
        for (StatisticKind statisticKind : values()) {
            if (statisticKind.getId().equals(statisticId)) {
                return statisticKind;
            }
        }

        return null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return I18n.get("statistics.wynntils." + id);
    }
}
