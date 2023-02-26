/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.utils.mc.McUtils;

public class MinecraftFunctions {
    public static class XFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return McUtils.player().getBlockX();
        }
    }

    public static class YFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return McUtils.player().getBlockY();
        }
    }

    public static class ZFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return McUtils.player().getBlockZ();
        }
    }

    public static class DirFunction extends Function<Double> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return McUtils.player().getYRot();
        }
    }

    public static class FpsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return MinecraftAccessor.getFps();
        }
    }
}
