/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.statistics.type;

import com.google.common.base.CaseFormat;
import com.wynntils.services.statistics.CustomStatFormatters;
import java.util.Locale;
import net.minecraft.client.resources.language.I18n;

public enum StatisticKind {
    // region Gameplay
    DAMAGE_DEALT(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    SPELLS_CAST(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    // endregion

    // region Lootruns
    MYTHICS_FOUND(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    LOOTRUNS_COMPLETED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    LOOTRUNS_FAILED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    // LOOTRUNS_CHALLENGES_COMPLETED is actually LOOTRUNS_PULLS_COMPLETED,
    // but we can't rename it without storage upfixers (i18n is already updated though)
    LOOTRUNS_CHALLENGES_COMPLETED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    LOOTRUNS_PULLS_WITHOUT_MYTHIC(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    LOOTRUNS_TIME_ELAPSED(CustomStatFormatters.TIME, StatisticType.ADVANCED),
    LOOTRUNS_REWARD_PULLS(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    LOOTRUNS_REWARD_REROLLS(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    LOOTRUNS_EXPERIENCE_GAINED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    LOOTRUNS_MOBS_KILLED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.ADVANCED),
    LOOTRUNS_CHESTS_OPENED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    // endregion

    // region Raids
    NEST_OF_THE_GROOTSLANGS_FAILED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    NEST_OF_THE_GROOTSLANGS_SUCCEEDED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    NEST_OF_THE_GROOTSLANGS_TIME_ELAPSED(CustomStatFormatters.TIME, StatisticType.ADVANCED),
    ORPHIONS_NEXUS_OF_LIGHT_FAILED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    ORPHIONS_NEXUS_OF_LIGHT_SUCCEEDED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    ORPHIONS_NEXUS_OF_LIGHT_TIME_ELAPSED(CustomStatFormatters.TIME, StatisticType.ADVANCED),
    THE_CANYON_COLOSSUS_FAILED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    THE_CANYON_COLOSSUS_SUCCEEDED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    THE_CANYON_COLOSSUS_TIME_ELAPSED(CustomStatFormatters.TIME, StatisticType.ADVANCED),
    THE_NAMELESS_ANOMALY_FAILED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    THE_NAMELESS_ANOMALY_SUCCEEDED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    THE_NAMELESS_ANOMALY_TIME_ELAPSED(CustomStatFormatters.TIME, StatisticType.ADVANCED),
    // endregion

    // region Wars
    WARS_JOINED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    // endregion

    // region World Events
    ANNIHILATIONS_COMPLETED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    ANNIHILATIONS_FAILED(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT),
    CORRUPTED_CACHES_FOUND(CustomStatFormatters.FORMATTED_NUMBER, StatisticType.COUNT);
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

    public String getFormattedValue(long value) {
        return formatter.format(value);
    }

    public enum StatisticType {
        COUNT, // only the count is relevant
        ADVANCED // min, max, average are all relevant
    }
}
