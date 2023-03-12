/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.functions.GenericFunction;
import com.wynntils.core.functions.arguments.FunctionArguments;
import java.util.List;

public class RandomFunctions {
    public static class RandomFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            int min = arguments.getArgument("min").getIntegerValue();
            int max = arguments.getArgument("max").getIntegerValue();
            return (int) ((Math.random() * (max - min)) + min);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("min", Integer.class, null),
                    new FunctionArguments.Argument<>("max", Integer.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("rand", "rnd");
        }
    }

    public static class RandomMaxFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            int min = arguments.getArgument("min").getIntegerValue();
            int max = arguments.getArgument("max").getIntegerValue();
            return (int) ((Math.random() * (max - min + 1)) + min);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("min", Integer.class, null),
                    new FunctionArguments.Argument<>("max", Integer.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("rand_max", "rndm", "rndx");
        }
    }
}
