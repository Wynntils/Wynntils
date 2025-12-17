/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.players.type.wynnplayer.CharacterData;
import com.wynntils.models.players.type.wynnplayer.WynnPlayerInfo;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CombatXpFunctions {
    public static class CappedLevelFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.CombatXp.getCombatLevel();
        }
    }

    public static class CappedXpFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.CombatXp.getXp();
        }
    }

    public static class XpPerMinuteRawFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return (int) (Models.CombatXp.getRawXpGainInLastMinute().stream()
                    .mapToDouble(Float::doubleValue)
                    .sum());
        }

        @Override
        protected List<String> getAliases() {
            return List.of("xpm_raw");
        }
    }

    public static class XpPerMinuteFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return StringUtils.integerToShortString((int) (Models.CombatXp.getRawXpGainInLastMinute().stream()
                    .mapToDouble(Float::doubleValue)
                    .sum()));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("xpm");
        }
    }

    public static class XpPercentagePerMinuteFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.CombatXp.getPercentageXpGainInLastMinute().stream()
                    .mapToDouble(Float::doubleValue)
                    .sum();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("xppm");
        }
    }

    public static class LevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CombatXp.getCombatLevel().current();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("lvl");
        }
    }

    public static class XpFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return StringUtils.integerToShortString(Models.CombatXp.getXp().current());
        }
    }

    public static class XpRawFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CombatXp.getXp().current();
        }
    }

    public static class XpReqFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return StringUtils.integerToShortString(Models.CombatXp.getXp().max());
        }
    }

    public static class XpReqRawFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CombatXp.getXp().max();
        }
    }

    public static class XpPctFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.CombatXp.getXp().getPercentage();
        }
    }

    public static class XpOverflowFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
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
}
