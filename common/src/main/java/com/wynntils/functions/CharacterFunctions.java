/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.player.LocalPlayer;

public class CharacterFunctions {
    public static class SoulpointFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Character.getSoulPoints();
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp");
        }
    }

    public static class SoulpointMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Character.getMaxSoulPoints();
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp_max");
        }
    }

    public static class BpsFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            LocalPlayer player = McUtils.player();
            double dX = player.getX() - player.xOld;
            double dZ = player.getZ() - player.zOld;
            double dY = player.getY() - player.yOld;
            return Math.sqrt((dX * dX) + (dZ * dZ) + (dY * dY)) * 20;
        }
    }

    public static class BpsXzFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            LocalPlayer player = McUtils.player();
            double dX = player.getX() - player.xOld;
            double dZ = player.getZ() - player.zOld;
            return Math.sqrt((dX * dX) + (dZ * dZ)) * 20;
        }
    }

    public static class SoulpointTimerFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            int totalSeconds = Models.Character.getTicksToNextSoulPoint() / 20;

            int seconds = totalSeconds % 60;
            int minutes = totalSeconds / 60;
            return String.format("%d:%02d", minutes, seconds);
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp_timer");
        }
    }

    public static class SoulpointTimerMFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            int totalSeconds = Models.Character.getTicksToNextSoulPoint() / 20;

            return totalSeconds / 60;
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp_timer_m");
        }
    }

    public static class SoulpointTimerSFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            int totalSeconds = Models.Character.getTicksToNextSoulPoint() / 20;

            return totalSeconds % 60;
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp_timer_s");
        }
    }

    public static class ClassFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Boolean showReskinnedName =
                    arguments.getArgument("showReskinnedName").getBooleanValue();

            String name = showReskinnedName
                    ? Models.Character.getActualName()
                    : Models.Character.getClassType().getActualName(false);

            if (arguments.getArgument("uppercase").getBooleanValue()) {
                return name.toUpperCase(Locale.ROOT);
            }

            return name;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("uppercase", Boolean.class, false),
                    new FunctionArguments.Argument<>("showReskinnedName", Boolean.class, true)));
        }
    }

    public static class ManaFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Character.getCurrentMana();
        }
    }

    public static class ManaMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Character.getMaxMana();
        }
    }

    public static class HealthFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Character.getCurrentHealth();
        }
    }

    public static class HealthMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Character.getMaxHealth();
        }
    }

    public static class HealthPctFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            int currentHealth = Models.Character.getCurrentHealth();
            int maxHealth = Models.Character.getMaxHealth();
            return (currentHealth / maxHealth * 100.0d);
        }
    }

    public static class ManaPctFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            int currentMana = Models.Character.getCurrentMana();
            int maxMana = Models.Character.getMaxMana();
            return (currentMana / maxMana * 100.0d);
        }
    }
}
