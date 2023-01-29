/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.ActiveFunction;
import com.wynntils.core.functions.Function;
import com.wynntils.models.experience.event.CombatXpGainEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.TimedSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CombatXpFunctions {
    public static class XpPerMinuteRawFunction extends ActiveFunction<Integer> {
        private static final TimedSet<Double> timedXpSet = new TimedSet<>(1, TimeUnit.MINUTES, true);

        @Override
        public Integer getValue(String argument) {
            return (int) (timedXpSet.stream().mapToDouble(Double::doubleValue).sum());
        }

        @SubscribeEvent
        public void onExperienceGain(CombatXpGainEvent event) {
            timedXpSet.put((double) event.getGainedXpRaw());
        }

        @Override
        public List<String> getAliases() {
            return List.of("xpm_raw");
        }
    }

    public static class XpPerMinuteFunction extends ActiveFunction<String> {
        private static final TimedSet<Double> timedXpSet = new TimedSet<>(1, TimeUnit.MINUTES, true);

        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString(
                    (int) (timedXpSet.stream().mapToDouble(Double::doubleValue).sum()));
        }

        @SubscribeEvent
        public void onExperienceGain(CombatXpGainEvent event) {
            timedXpSet.put((double) event.getGainedXpRaw());
        }

        @Override
        public List<String> getAliases() {
            return List.of("xpm");
        }
    }

    public static class XpPercentagePerMinuteFunction extends ActiveFunction<Double> {
        private static final TimedSet<Double> timedXpSet = new TimedSet<>(1, TimeUnit.MINUTES, true);

        @Override
        public Double getValue(String argument) {
            // Round to 2 decimal places
            return Math.round(
                            timedXpSet.stream().mapToDouble(Double::doubleValue).sum() * 100.0)
                    / 100.0;
        }

        @SubscribeEvent
        public void onExperienceGain(CombatXpGainEvent event) {
            timedXpSet.put((double) event.getGainedXpPercentage());
        }

        @Override
        public List<String> getAliases() {
            return List.of("xppm");
        }
    }

    public static class LevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.CombatXp.getXpLevel();
        }

        @Override
        public List<String> getAliases() {
            return List.of("lvl");
        }
    }

    public static class XpFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString((int) Models.CombatXp.getCurrentXp());
        }
    }

    public static class XpRawFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            return Models.CombatXp.getCurrentXp();
        }
    }

    public static class XpReqFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString(Models.CombatXp.getXpPointsNeededToLevelUp());
        }
    }

    public static class XpReqRawFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.CombatXp.getXpPointsNeededToLevelUp();
        }
    }

    public static class XpPctFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            return Models.CombatXp.getXpProgress() * 100.0f;
        }
    }
}
