/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.DependantFunction;
import com.wynntils.core.functions.Function;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;

public class CharacterFunctions {
    public static class SoulpointFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getSoulPoints();
        }

        @Override
        public List<String> getAliases() {
            return List.of("sp");
        }
    }

    public static class SoulpointMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getMaxSoulPoints();
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
            int totalSeconds = Managers.Character.getCharacterInfo().getTicksToNextSoulPoint() / 20;

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
            int totalSeconds = Managers.Character.getCharacterInfo().getTicksToNextSoulPoint() / 20;

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
            int totalSeconds = Managers.Character.getCharacterInfo().getTicksToNextSoulPoint() / 20;

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
            return Managers.Character.getCharacterInfo().getActualName();
        }
    }

    public static class LiquidEmeraldFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            int ems = Models.PlayerInventory.getCurrentEmeraldCount();
            return ems / 4096;
        }

        @Override
        public List<String> getAliases() {
            return List.of("le");
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.PlayerInventory);
        }
    }

    public static class EmeraldBlockFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            int ems = Models.PlayerInventory.getCurrentEmeraldCount();
            return (ems % 4096) / 64;
        }

        @Override
        public List<String> getAliases() {
            return List.of("eb");
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.PlayerInventory);
        }
    }

    public static class EmeraldsFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.PlayerInventory.getCurrentEmeraldCount() % 64;
        }

        @Override
        public List<String> getAliases() {
            return List.of("em");
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.PlayerInventory);
        }
    }

    public static class MoneyFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.PlayerInventory.getCurrentEmeraldCount();
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.PlayerInventory);
        }
    }

    public static class InventoryFreeFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.PlayerInventory.getOpenInvSlots();
        }

        @Override
        public List<String> getAliases() {
            return List.of("inv_free");
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.PlayerInventory);
        }
    }

    public static class InventoryUsedFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.PlayerInventory.getUsedInvSlots();
        }

        @Override
        public List<String> getAliases() {
            return List.of("inv_used");
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.PlayerInventory);
        }
    }

    public static class ManaFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.ActionBar.getCurrentMana();
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.ActionBar);
        }
    }

    public static class ManaMaxFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.ActionBar.getMaxMana();
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.ActionBar);
        }
    }

    public static class HealthFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.ActionBar.getCurrentHealth();
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.ActionBar);
        }
    }

    public static class HealthMaxFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.ActionBar.getMaxHealth();
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.ActionBar);
        }
    }

    public static class HealthPctFunction extends DependantFunction<Float> {
        @Override
        public Float getValue(String argument) {
            int currentHealth = Models.ActionBar.getCurrentHealth();
            int maxHealth = Models.ActionBar.getMaxHealth();
            return ((float) currentHealth / maxHealth * 100.0f);
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.ActionBar);
        }
    }

    public static class ManaPctFunction extends DependantFunction<Float> {
        @Override
        public Float getValue(String argument) {
            int currentMana = Models.ActionBar.getCurrentMana();
            int maxMana = Models.ActionBar.getMaxMana();
            return ((float) currentMana / maxMana * 100.0f);
        }

        @Override
        public List<Model> getModelDependencies() {
            return List.of(Models.ActionBar);
        }
    }

    public static class LevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getLevel();
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
                    (int) Managers.Character.getCharacterInfo().getCurrentXp());
        }
    }

    public static class XpRawFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            return Managers.Character.getCharacterInfo().getCurrentXp();
        }
    }

    public static class XpReqFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString(
                    Managers.Character.getCharacterInfo().getXpPointsNeededToLevelUp());
        }
    }

    public static class XpReqRawFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getXpPointsNeededToLevelUp();
        }
    }

    public static class XpPctFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            return Managers.Character.getCharacterInfo().getXpProgress() * 100.0f;
        }
    }

    public static class WoodcuttingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.WOODCUTTING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("woodcutting");
        }
    }

    public static class MiningLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.MINING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("mining");
        }
    }

    public static class FishingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.FISHING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("fishing");
        }
    }

    public static class FarmingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.FARMING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("farming");
        }
    }

    public static class AlchemismLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.ALCHEMISM);
        }

        @Override
        public List<String> getAliases() {
            return List.of("alchemism");
        }
    }

    public static class ArmouringLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.ARMOURING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("armouring");
        }
    }

    public static class CookingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.COOKING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("cooking");
        }
    }

    public static class JewelingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.JEWELING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("jeweling");
        }
    }

    public static class ScribingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.SCRIBING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("scribing");
        }
    }

    public static class TailoringLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.TAILORING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("tailoring");
        }
    }

    public static class WeaponsmithingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.WEAPONSMITHING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("weaponsmithing");
        }
    }

    public static class WoodworkingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Managers.Character.getCharacterInfo().getProfessionInfo().getLevel(ProfessionType.WOODWORKING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("woodworking");
        }
    }
}
