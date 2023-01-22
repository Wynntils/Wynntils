/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.models.concepts.ProfessionType;
import com.wynntils.utils.StringUtils;
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

    public static class LiquidEmeraldFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            int ems = Models.Emerald.getAmountInInventory();
            return ems / 4096;
        }

        @Override
        public List<String> getAliases() {
            return List.of("le");
        }
    }

    public static class EmeraldBlockFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            int ems = Models.Emerald.getAmountInInventory();
            return (ems % 4096) / 64;
        }

        @Override
        public List<String> getAliases() {
            return List.of("eb");
        }
    }

    public static class EmeraldsFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Emerald.getAmountInInventory() % 64;
        }

        @Override
        public List<String> getAliases() {
            return List.of("em");
        }
    }

    public static class MoneyFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Emerald.getAmountInInventory();
        }
    }

    public static class InventoryFreeFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.PlayerInventory.getOpenInvSlots();
        }

        @Override
        public List<String> getAliases() {
            return List.of("inv_free");
        }
    }

    public static class InventoryUsedFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.PlayerInventory.getUsedInvSlots();
        }

        @Override
        public List<String> getAliases() {
            return List.of("inv_used");
        }
    }

    public static class ManaFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.ActionBar.getCurrentMana();
        }
    }

    public static class ManaMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.ActionBar.getMaxMana();
        }
    }

    public static class HealthFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.ActionBar.getCurrentHealth();
        }
    }

    public static class HealthMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.ActionBar.getMaxHealth();
        }
    }

    public static class HealthPctFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            int currentHealth = Models.ActionBar.getCurrentHealth();
            int maxHealth = Models.ActionBar.getMaxHealth();
            return ((float) currentHealth / maxHealth * 100.0f);
        }
    }

    public static class ManaPctFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            int currentMana = Models.ActionBar.getCurrentMana();
            int maxMana = Models.ActionBar.getMaxMana();
            return ((float) currentMana / maxMana * 100.0f);
        }
    }

    public static class LevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getXpLevel();
        }

        @Override
        public List<String> getAliases() {
            return List.of("lvl");
        }
    }

    public static class XpFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString((int) Models.Character.getCurrentXp());
        }
    }

    public static class XpRawFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            return Models.Character.getCurrentXp();
        }
    }

    public static class XpReqFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString(Models.Character.getXpPointsNeededToLevelUp());
        }
    }

    public static class XpReqRawFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Character.getXpPointsNeededToLevelUp();
        }
    }

    public static class XpPctFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            return Models.Character.getXpProgress() * 100.0f;
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
