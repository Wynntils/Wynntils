/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.character.type.VehicleType;
import com.wynntils.models.objectives.WynnObjective;
import com.wynntils.services.leaderboard.type.LeaderboardType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.NamedValue;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.player.LocalPlayer;

public class CharacterFunctions {
    public static class CappedManaFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY);
        }
    }

    public static class CappedHealthFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY);
        }
    }

    public static class SprintFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getSprint().orElse(CappedValue.EMPTY);
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
                    new Argument<>("uppercase", Boolean.class, false),
                    new Argument<>("showReskinnedName", Boolean.class, true)));
        }
    }

    public static class ManaFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY).current();
        }
    }

    public static class ManaMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY).max();
        }
    }

    public static class HealthFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY).current();
        }
    }

    public static class HealthMaxFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY).max();
        }
    }

    public static class HealthPctFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY).getPercentage();
        }
    }

    public static class ManaPctFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY).getPercentage();
        }
    }

    public static class IdFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Character.getId();
        }
    }

    public static class CappedAwakenedProgressFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.awakenedBar.isActive()
                    ? Models.Ability.awakenedBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class CappedBloodPoolFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.bloodPoolBar.isActive()
                    ? Models.Ability.bloodPoolBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class CappedCorruptedFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.corruptedBar.isActive()
                    ? Models.Ability.corruptedBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class CappedFocusFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.focusBar.isActive()
                    ? Models.Ability.focusBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class CappedManaBankFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.manaBankBar.isActive()
                    ? Models.Ability.manaBankBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class CappedOphanimFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.ophanimBar.isActive()
                    ? Models.Ability.ophanimBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }
    }

    public static class OphanimOrb extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            int orbNumber = arguments.getArgument("orbNumber").getIntegerValue();
            return orbNumber < Models.Ability.ophanimBar.getOrbs().size() && orbNumber >= 0
                    ? Models.Ability.ophanimBar.getOrbs().get(orbNumber).getHealthState()
                    : -1;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("orbNumber", Integer.class, null)));
        }
    }

    public static class OphanimActive extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Ability.ophanimBar.isActive();
        }
    }

    public static class CappedHolyPowerFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.holyPowerBar.isActive()
                    ? Models.Ability.holyPowerBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }

        @Override
        protected List<String> getAliases() {
            // Old function name before ability rename, keep to not break old functions
            return List.of("capped_sacred_surge");
        }
    }

    public static class CommanderDurationFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Ability.commanderBar.isActive() ? Models.Ability.commanderBar.getDuration() : 0;
        }
    }

    public static class CommanderActivatedFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Ability.commanderBar.isActive() && Models.Ability.commanderBar.isActivated();
        }
    }

    public static class MomentumPercentFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Ability.momentumBar.isActive()
                    ? Models.Ability.momentumBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }

        @Override
        protected List<String> getAliases() {
            return List.of("momentum_pct");
        }
    }

    public static class MomentumFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Ability.momentumBar.getMomentum();
        }
    }

    public static class IsRidingHorseFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Character.getVehicle() == VehicleType.HORSE;
        }
    }

    public static class HasNoGuiFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Character.getVehicle() == VehicleType.DISPLAY;
        }
    }

    public static class HummingbirdsStateFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Ability.hummingBirdsState;
        }
    }

    public static class OphanimHealingPercentFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Ability.ophanimBar.isActive() ? Models.Ability.ophanimBar.getHealed() : -1;
        }
    }

    public static class GuildObjectiveScoreFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            WynnObjective weekly = Models.Objectives.getGuildObjective();
            if (weekly == null) return CappedValue.EMPTY;
            return weekly.getScore();
        }
    }

    public static class GuildObjectiveGoalFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            WynnObjective weekly = Models.Objectives.getGuildObjective();
            if (weekly == null) return "";
            return weekly.getGoal();
        }
    }

    public static class GuildObjectiveEventBonusFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            WynnObjective weekly = Models.Objectives.getGuildObjective();
            if (weekly == null) return false;
            return weekly.hasEventBonus();
        }
    }

    public static class PersonalObjectiveScoreFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            int index = arguments.getArgument("index").getIntegerValue();
            List<WynnObjective> daily = Models.Objectives.getPersonalObjectives();
            return !daily.isEmpty() && index >= 0 && daily.size() > index
                    ? daily.get(index).getScore()
                    : CappedValue.EMPTY;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(List.of(new Argument<>("index", Integer.class, 0)));
        }
    }

    public static class PersonalObjectiveGoalFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            int index = arguments.getArgument("index").getIntegerValue();
            List<WynnObjective> daily = Models.Objectives.getPersonalObjectives();
            return !daily.isEmpty() && index >= 0 && daily.size() > index
                    ? daily.get(index).getGoal()
                    : "";
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(List.of(new Argument<>("index", Integer.class, 0)));
        }
    }

    public static class PersonalObjectiveEventBonusFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            int index = arguments.getArgument("index").getIntegerValue();
            List<WynnObjective> daily = Models.Objectives.getPersonalObjectives();
            return !daily.isEmpty()
                    && index >= 0
                    && daily.size() > index
                    && daily.get(index).hasEventBonus();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(List.of(new Argument<>("index", Integer.class, 0)));
        }
    }

    public static class EquippedAspectFunction extends Function<NamedValue> {
        @Override
        public NamedValue getValue(FunctionArguments arguments) {
            int aspectIndex = arguments.getArgument("index").getIntegerValue();
            Optional<String> equippedAspectOpt = Models.Aspect.getEquippedAspect(aspectIndex);
            if (equippedAspectOpt.isEmpty()) return NamedValue.EMPTY;

            Optional<Integer> aspectTierOpt = Models.Aspect.getAspectTierByName(equippedAspectOpt.get());
            return aspectTierOpt
                    .map(s -> new NamedValue(equippedAspectOpt.get(), aspectTierOpt.get()))
                    .orElse(NamedValue.EMPTY);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("index", Integer.class, null)));
        }
    }

    public static class IsAspectEquippedFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            String aspectName = arguments.getArgument("aspectName").getStringValue();
            return Models.Aspect.getEquippedAspectByName(aspectName).isPresent();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("aspectName", String.class, null)));
        }
    }

    public static class AspectTierFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            String aspectName = arguments.getArgument("aspectName").getStringValue();
            return Models.Aspect.getAspectTierByName(aspectName).orElse(0);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("aspectName", String.class, null)));
        }
    }

    public static class LeaderboardPositionFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            String leaderboardKey = arguments.getArgument("leaderboardKey").getStringValue();
            LeaderboardType leaderboardType = LeaderboardType.fromKey(leaderboardKey);

            if (leaderboardType == null) return 0;

            return Models.Account.getPlayerInfo().leaderboardPlacements().getOrDefault(leaderboardType, 0);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("leaderboardKey", String.class, null)));
        }
    }
}
