/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.DependantFunction;
import com.wynntils.core.functions.Function;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.model.ActionBarModel;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.model.PlayerInventoryModel;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;

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
            int totalSeconds = CharacterManager.getCharacterInfo().getTicksToNextSoulPoint() / 20;

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
            int totalSeconds = CharacterManager.getCharacterInfo().getTicksToNextSoulPoint() / 20;

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
            int totalSeconds = CharacterManager.getCharacterInfo().getTicksToNextSoulPoint() / 20;

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
            return CharacterManager.getCharacterInfo().getActualName();
        }
    }

    public static class LiquidEmeraldFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            int ems = PlayerInventoryModel.getCurrentEmeraldCount();
            return ems / 4096;
        }

        @Override
        public List<String> getAliases() {
            return List.of("le");
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(PlayerInventoryModel.class);
        }
    }

    public static class EmeraldBlockFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            int ems = PlayerInventoryModel.getCurrentEmeraldCount();
            return (ems % 4096) / 64;
        }

        @Override
        public List<String> getAliases() {
            return List.of("eb");
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(PlayerInventoryModel.class);
        }
    }

    public static class EmeraldsFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return PlayerInventoryModel.getCurrentEmeraldCount() % 64;
        }

        @Override
        public List<String> getAliases() {
            return List.of("em");
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(PlayerInventoryModel.class);
        }
    }

    public static class MoneyFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return PlayerInventoryModel.getCurrentEmeraldCount();
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(PlayerInventoryModel.class);
        }
    }

    public static class InventoryFreeFunction extends DependantFunction<Integer> {
        @Override
        public Integer getValue(String argument) {
            return PlayerInventoryModel.getOpenInvSlots();
        }

        @Override
        public List<String> getAliases() {
            return List.of("inv_free");
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(PlayerInventoryModel.class);
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

    public static class ManaPctFunction extends DependantFunction<Float> {
        @Override
        public Float getValue(String argument) {
            int currentMana = ActionBarModel.getCurrentMana();
            int maxMana = ActionBarModel.getMaxMana();
            return ((float) currentMana / maxMana * 100.0f);
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
