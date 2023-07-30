/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.statistics.type;

import com.google.common.base.CaseFormat;
import com.wynntils.services.statistics.CustomStatFormatters;
import java.util.Locale;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.stats.StatFormatter;

public enum StatisticKind {
    DAMAGE_DEALT(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    SPELLS_CAST(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),

    // region Lootruns

    LOOTRUNS_COMPLETED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    LOOTRUNS_FAILED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    LOOTRUNS_CHALLENGES_COMPLETED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    LOOTRUNS_TIME_ELAPSED(CustomStatFormatters.TIME, StatisticType.ADVANCED),
    LOOTRUNS_REWARD_PULLS(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    LOOTRUNS_REWARD_REROLLS(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    LOOTRUNS_EXPERIENCE_GAINED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    LOOTRUNS_MOBS_KILLED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED);

    // endregion

    private final StatFormatter formatter;
    private final StatisticType type;
    private final String id;

    StatisticKind(StatFormatter formatter, StatisticType type) {
        this.formatter = formatter;
        this.type = type;
        this.id = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name().toLowerCase(Locale.ROOT));
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

    public StatisticType getType() {
        return type;
    }

    public String getName() {
        return I18n.get("statistics.wynntils." + id + ".name");
    }

    public String getFormattedValue(int value) {
        return formatter.format(value);
    }

    public enum StatisticType {
        COUNT, // only the count is relevant
        ADVANCED // min, max, average are all relevant
    }
}
