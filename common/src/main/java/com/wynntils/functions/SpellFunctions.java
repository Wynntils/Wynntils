/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
import java.util.List;
import net.minecraft.ChatFormatting;

public class SpellFunctions {
    public static class ArrowShieldCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.ArrowShield.getArrowShieldCharge();
        }

        @Override
        public List<String> getAliases() {
            return List.of("arrow_shield");
        }
    }

    public static class ShamanMaskFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            ChatFormatting color = arguments.getArgument("isColored").getBooleanValue()
                    ? Models.ShamanMask.getCurrentMaskType().getColor()
                    : ChatFormatting.WHITE;

            if (arguments.getArgument("useShortName").getBooleanValue()) {
                return color + Models.ShamanMask.getCurrentMaskType().getAlias();
            }

            return color + Models.ShamanMask.getCurrentMaskType().getName();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("isColored", Boolean.class, true),
                    new FunctionArguments.Argument<>("useShortName", Boolean.class, false)));
        }
    }
}
