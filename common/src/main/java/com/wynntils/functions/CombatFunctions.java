/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.core.BlockPos;

public class CombatFunctions {
    public static class AreaDamagePerSecondFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Damage.getAreaDamagePerSecond();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("adps");
        }
    }

    public static class AreaDamageAverageFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.Damage.getAverageAreaDamagePerSecond(
                    arguments.getArgument("seconds").getIntegerValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("seconds", Integer.class, 10)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("adavg");
        }
    }

    public static class HeightFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            // iteratively find the first non-air block below the player
            double endY = (int) Math.ceil(McUtils.player().position().y) - 1;
            while (McUtils.mc()
                    .level
                    .getBlockState(new BlockPos(
                            McUtils.player().blockPosition().getX(),
                            (int) endY,
                            McUtils.player().blockPosition().getZ()))
                    .isAir()) {
                endY--;
            }

            // add the floor height to the result to account for half-blocks
            endY += McUtils.mc()
                    .level
                    .getBlockFloorHeight(new BlockPos(
                            McUtils.player().blockPosition().getX(),
                            (int) endY,
                            McUtils.player().blockPosition().getZ()));
            return McUtils.player().position().y - endY;
        }

        @Override
        protected List<String> getAliases() {
            return List.of("agl", "altitude");
        }
    }
}
