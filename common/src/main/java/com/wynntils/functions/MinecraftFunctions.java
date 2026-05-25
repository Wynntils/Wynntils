/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.wynn.RaycastUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.IdentifierException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

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

    public static class LocationAtCrosshairFunction extends Function<Location> {
        @Override
        public Location getValue(FunctionArguments arguments) {
            double maxDistance = arguments.getArgument("distance").getDoubleValue();
            Optional<BlockPos> hitBlock = RaycastUtils.getTargetedBlock(maxDistance);

            if (hitBlock.isEmpty()) return new Location(0, 0, 0);

            return new Location(hitBlock.get());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("distance", Double.class, 50.0)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("crosshair_loc");
        }
    }

    public static class PitchFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return (double) McUtils.player().getXRot();
        }
    }

    public static class YawFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double rawYawAngle = (double) McUtils.player().getYRot();
            return Mth.wrapDegrees(rawYawAngle);
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

    public static class TicksFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return McUtils.mc().level.getGameTime();
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
                    List.of(new Argument<>("keyCode", Integer.class, null)));
        }
    }

    public static class MinecraftEffectDurationFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            String effectName = arguments.getArgument("effectName").getStringValue();
            Identifier effectLocation;
            try {
                effectLocation = Identifier.withDefaultNamespace(effectName);
            } catch (IdentifierException e) {
                return -1; // Effect name contains invalid characters
            }

            Holder<MobEffect> effectHolder =
                    BuiltInRegistries.MOB_EFFECT.get(effectLocation).orElse(null);

            if (effectHolder == null) return -1; // Effect holder not found

            // Check if the player has the effect
            if (McUtils.player().hasEffect(effectHolder)) {
                MobEffectInstance effectInstance = McUtils.player().getEffect(effectHolder);

                if (effectInstance != null && effectInstance.getDuration() >= 0) {
                    return effectInstance.getDuration();
                }
            }

            return -1; // Effect not active
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("effectName", String.class, null)));
        }
    }
}
