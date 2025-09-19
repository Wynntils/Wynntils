/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.abilities.type.ShamanTotem;
import com.wynntils.models.abilities.type.ShieldType;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.PosUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;

public class SpellFunctions {
    public static class ArrowShieldCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            if (Models.Character.getClassType() != ClassType.ARCHER) return 0;

            return Models.Shield.getShieldCharge();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("arrow_shield");
        }
    }

    public static class GuardianAngelsCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            if (Models.Character.getClassType() != ClassType.ARCHER) return 0;

            return Models.Shield.getShieldCharge();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("guardian_angels");
        }
    }

    public static class MantleShieldCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            if (Models.Character.getClassType() != ClassType.WARRIOR) return 0;

            return Models.Shield.getShieldCharge();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mantle_shield");
        }
    }

    public static class ShieldTypeNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            ShieldType shieldType = Models.Shield.getActiveShieldType();

            if (shieldType == null) return "";

            return shieldType.getName();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("shield_type");
        }
    }

    public static class ShamanMaskFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            ChatFormatting color = arguments.getArgument("isColored").getBooleanValue()
                    ? Models.ShamanMask.getCurrentMaskType().getColor()
                    : ChatFormatting.WHITE;

            Boolean useShortName = arguments.getArgument("useShortName").getBooleanValue();
            String name = useShortName
                    ? Models.ShamanMask.getCurrentMaskType().getAlias()
                    : Models.ShamanMask.getCurrentMaskType().getName();

            return color + name;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(List.of(
                    new Argument<>("isColored", Boolean.class, true),
                    new Argument<>("useShortName", Boolean.class, false)));
        }
    }

    public static class ShamanTotemStateFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            int totemNumber = arguments.getArgument("totemNumber").getIntegerValue();

            ShamanTotem shamanTotem = Models.ShamanTotem.getTotem(totemNumber);

            if (shamanTotem == null) {
                return "";
            }

            return shamanTotem.getState().toString().toUpperCase(Locale.ROOT);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class ShamanTotemLocationFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            int totemNumber = arguments.getArgument("totemNumber").getIntegerValue();

            ShamanTotem shamanTotem = Models.ShamanTotem.getTotem(totemNumber);

            if (shamanTotem == null) {
                return "";
            }

            return Location.containing(shamanTotem.getPosition()).toString();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class ShamanTotemTimeLeftFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            int totemNumber = arguments.getArgument("totemNumber").getIntegerValue();

            ShamanTotem shamanTotem = Models.ShamanTotem.getTotem(totemNumber);

            if (shamanTotem == null) {
                return 0;
            }

            return shamanTotem.getTime();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class ShamanTotemDistanceFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            int totemNumber = arguments.getArgument("totemNumber").getIntegerValue();

            ShamanTotem shamanTotem = Models.ShamanTotem.getTotem(totemNumber);

            if (shamanTotem == null) {
                return 0d;
            }

            return McUtils.player().position().distanceTo(PosUtils.toVec3(shamanTotem.getPosition()));
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }
}
