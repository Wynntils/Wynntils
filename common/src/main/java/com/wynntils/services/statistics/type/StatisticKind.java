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
    DAMAGE_DEALT(StatFormatter.DEFAULT),
    SPELLS_CAST(StatFormatter.DEFAULT),

    // region Lootruns

    LOOTRUNS_COMPLETED(StatFormatter.DEFAULT),
    LOOTRUNS_FAILED(StatFormatter.DEFAULT),
    LOOTRUNS_CHALLENGES_COMPLETED(StatFormatter.DEFAULT),
    LOOTRUNS_TIME_ELAPSED(CustomStatFormatters.TIME),
    LOOTRUNS_REWARD_PULLS(StatFormatter.DEFAULT),
    LOOTRUNS_REWARD_REROLLS(StatFormatter.DEFAULT),
    LOOTRUNS_EXPERIENCE_GAINED(StatFormatter.DEFAULT),
    LOOTRUNS_MOBS_KILLED(StatFormatter.DEFAULT);

    // endregion

    private final StatFormatter formatter;
    private final String id;

    StatisticKind(StatFormatter formatter) {
        this.formatter = formatter;
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

    public String getName() {
        return I18n.get("statistics.wynntils." + id + ".name");
    }

    public String getFormattedValue(int value) {
        return formatter.format(value);
    }
}
