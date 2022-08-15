/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.DependantFunction;
import com.wynntils.core.functions.Function;
import com.wynntils.core.managers.Model;
import com.wynntils.utils.StringUtils;
import com.wynntils.wc.model.ActionBarModel;
import com.wynntils.wc.model.CharacterManager;
import java.util.List;

public class CharacterFunctions {
    public static class SoulpointFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return CharacterManager.getCharacterInfo().getSoulPoints();
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp");
        }
    }

    public static class SoulpointMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return CharacterManager.getCharacterInfo().getMaxSoulPoints();
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp_max");
        }
    }

    public static class SoulpointTimerFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            int totalseconds = CharacterManager.getCharacterInfo().getTicksToNextSoulPoint() / 20;

            int seconds = totalseconds % 60;
            int minutes = totalseconds / 60;
            return String.format("%d:%02d", minutes, seconds);
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp_timer");
        }
    }

    public static class SoulpointTimerMFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            int totalseconds = CharacterManager.getCharacterInfo().getTicksToNextSoulPoint() / 20;

            int minutes = totalseconds / 60;
            return minutes;
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp_timer_m");
        }
    }

    public static class SoulpointTimerSFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            int totalseconds = CharacterManager.getCharacterInfo().getTicksToNextSoulPoint() / 20;

            int seconds = totalseconds % 60;
            return seconds;
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp_timer_s");
        }
    }

    public static class ClassFunction extends Function<String> {
        // FIXME: original had upper/lower case versions. Make a upper/lower function instead.
        @Override
        public String getValue(String argument) {
            return CharacterManager.getCharacterInfo().getActualName();
        }
    }

    public static class ManaFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return ActionBarModel.getCurrentMana();
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ActionBarModel.class);
        }
    }

    public static class ManaMaxFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return ActionBarModel.getMaxMana();
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ActionBarModel.class);
        }
    }

    public static class HealthFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return ActionBarModel.getCurrentHealth();
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ActionBarModel.class);
        }
    }

    public static class HealthMaxFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return ActionBarModel.getMaxHealth();
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ActionBarModel.class);
        }
    }

    public static class HealthPctFunction extends DependantFunction<Float> {
        @Override
        public Float getValue(String argument) {
            int currentHealth = ActionBarModel.getCurrentHealth();
            int maxHealth = ActionBarModel.getMaxHealth();
            return ((float) currentHealth / maxHealth * 100.0f);
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ActionBarModel.class);
        }
    }

    public static class LevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return CharacterManager.getCharacterInfo().getLevel();
        }

        @Override
        public List<String> getAliases() {
            return List.of("lvl");
        }
    }

    public static class XpFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString(
                    (int) CharacterManager.getCharacterInfo().getCurrentXp());
        }
    }

    public static class XpRawFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            return CharacterManager.getCharacterInfo().getCurrentXp();
        }
    }

    public static class XpReqFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString(
                    CharacterManager.getCharacterInfo().getXpPointsNeededToLevelUp());
        }
    }

    public static class XpReqRawFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return CharacterManager.getCharacterInfo().getXpPointsNeededToLevelUp();
        }
    }

    public static class XpPctFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            return CharacterManager.getCharacterInfo().getXpProgress() * 100.0f;
        }
    }
}
