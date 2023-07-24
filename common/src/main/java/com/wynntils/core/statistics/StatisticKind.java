/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.statistics;

import java.util.Locale;
import net.minecraft.client.resources.language.I18n;

public enum StatisticKind {
    DAMAGE_DEALT,
    SPELLS_CAST;

    private final String id;

    StatisticKind() {
        this.id = name().toLowerCase(Locale.ROOT);
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
