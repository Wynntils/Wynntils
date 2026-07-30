/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.players.type.wynnplayer.CharacterData;
import com.wynntils.models.players.type.wynnplayer.WynnPlayerInfo;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class CombatXpFunctions {
    @TemplateFunction(name = "capped_level")
    public static CappedValue cappedLevelFunction() {
        return Models.CombatXp.getCombatLevel();
    }

    @TemplateFunction(name = "capped_xp")
    public static CappedValue cappedXpFunction() {
        return Models.CombatXp.getXp();
    }

    @TemplateFunction(name = "xp_per_minute_raw", aliases = "xpm_raw")
    public static int xpPerMinuteRawFunction() {
        return (int) (Models.CombatXp.getRawXpGainInLastMinute().stream()
                .mapToDouble(Float::doubleValue)
                .sum());
    }

    @TemplateFunction(name = "xp_per_minute", aliases = "xpm")
    public static String xpPerMinuteFunction() {
        return StringUtils.integerToShortString((int) (Models.CombatXp.getRawXpGainInLastMinute().stream()
                .mapToDouble(Float::doubleValue)
                .sum()));
    }

    @TemplateFunction(name = "xp_percentage_per_minute", aliases = "xppm")
    public static double xpPercentagePerMinuteFunction() {
        return Models.CombatXp.getPercentageXpGainInLastMinute().stream()
                .mapToDouble(Float::doubleValue)
                .sum();
    }

    @TemplateFunction(name = "level", aliases = "lvl")
    public static int levelFunction() {
        return Models.CombatXp.getCombatLevel().current();
    }

    @TemplateFunction(name = "xp")
    public static String xpFunction() {
        return StringUtils.integerToShortString(Models.CombatXp.getXp().current());
    }

    @TemplateFunction(name = "xp_raw")
    public static int xpRawFunction() {
        return Models.CombatXp.getXp().current();
    }

    @TemplateFunction(name = "xp_req")
    public static String xpReqFunction() {
        return StringUtils.integerToShortString(Models.CombatXp.getXp().max());
    }

    @TemplateFunction(name = "xp_req_raw")
    public static int xpReqRawFunction() {
        return Models.CombatXp.getXp().max();
    }

    @TemplateFunction(name = "xp_percentage", aliases = "xp_pct")
    public static double xpPercentageFunction() {
        return Models.CombatXp.getXp().getPercentage();
    }

    @TemplateFunction(name = "xp_overflow")
    public static long xpOverflowFunction() {
        if (!Models.CombatXp.getCombatLevel().isAtCap()) return 0L;

        WynnPlayerInfo playerInfo = Models.Account.getPlayerInfo();
        CharacterData characterData;
        // Use the id we parsed to find the active character instead of what the API has as the active
        // character as that will be outdated until the next call to the API
        Optional<UUID> activeCharacterUuid = playerInfo.characters().keySet().stream()
                .filter(uuid -> uuid.toString().startsWith(Models.Character.getId()))
                .findFirst();

        if (activeCharacterUuid.isEmpty()) return 0L;

        characterData = playerInfo.characters().get(activeCharacterUuid.get());
        if (characterData == null) return 0L;

        return characterData.xp();
    }
}
