/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.List;

public class MinecraftFunctions {
    public static class MyLocationFunction extends Function<Location> {
        @Override
        public Location getValue(FunctionArguments arguments) {
            return new Location(McUtils.player().blockPosition());
        }

        @Override
        protected List<String> getAliases() {
            return List.of("my_loc");
        }
    }

    public static class DirFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return (double) McUtils.player().getYRot();
        }
    }

    public static class FpsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return MinecraftAccessor.getFps();
        }
    }

    public static class TicksFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return (int) McUtils.mc().level.getGameTime();
        }
    }

    public static class KeyPressedFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            int keyCode = arguments.getArgument("keyCode").getIntegerValue();
            return KeyboardUtils.isKeyDown(keyCode);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("keyCode", Integer.class, null)));
        }
    }
}
