/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.models.concepts.ProfessionType;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;

public class CharacterFunctions {
    public static class SoulpointFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getSoulPoints();
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp");
        }
    }

    public static class SoulpointMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getMaxSoulPoints();
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp_max");
        }
    }

    public static class BpsFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            LocalPlayer player = McUtils.player();
            double dX = player.getX() - player.xOld;
            double dZ = player.getZ() - player.zOld;
            double dY = player.getY() - player.yOld;
            return (float) Math.sqrt((dX * dX) + (dZ * dZ) + (dY * dY)) * 20;
        }
    }

    public static class BpsXzFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            LocalPlayer player = McUtils.player();
            double dX = player.getX() - player.xOld;
            double dZ = player.getZ() - player.zOld;
            return (float) Math.sqrt((dX * dX) + (dZ * dZ)) * 20;
        }
    }

    public static class SoulpointTimerFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
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
        public Integer getValue(String argument) {
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
        public Integer getValue(String argument) {
            int totalSeconds = Models.Character.getTicksToNextSoulPoint() / 20;

            return totalSeconds % 60;
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
            return Models.Character.getActualName();
        }
    }

    public static class ManaFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getCurrentMana();
        }
    }

    public static class ManaMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getMaxMana();
        }
    }

    public static class HealthFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getCurrentHealth();
        }
    }

    public static class HealthMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getMaxHealth();
        }
    }

    public static class HealthPctFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            int currentHealth = Models.Character.getCurrentHealth();
            int maxHealth = Models.Character.getMaxHealth();
            return ((float) currentHealth / maxHealth * 100.0f);
        }
    }

    public static class ManaPctFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            int currentMana = Models.Character.getCurrentMana();
            int maxMana = Models.Character.getMaxMana();
            return ((float) currentMana / maxMana * 100.0f);
        }
    }

    public static class WoodcuttingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.WOODCUTTING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("woodcutting");
        }
    }

    public static class MiningLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.MINING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("mining");
        }
    }

    public static class FishingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.FISHING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("fishing");
        }
    }

    public static class FarmingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.FARMING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("farming");
        }
    }

    public static class AlchemismLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.ALCHEMISM);
        }

        @Override
        public List<String> getAliases() {
            return List.of("alchemism");
        }
    }

    public static class ArmouringLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.ARMOURING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("armouring");
        }
    }

    public static class CookingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.COOKING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("cooking");
        }
    }

    public static class JewelingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.JEWELING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("jeweling");
        }
    }

    public static class ScribingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.SCRIBING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("scribing");
        }
    }

    public static class TailoringLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.TAILORING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("tailoring");
        }
    }

    public static class WeaponsmithingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.WEAPONSMITHING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("weaponsmithing");
        }
    }

    public static class WoodworkingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getProfessionInfo().getLevel(ProfessionType.WOODWORKING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("woodworking");
        }
    }
}
