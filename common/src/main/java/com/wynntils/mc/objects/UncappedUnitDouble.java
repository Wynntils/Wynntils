/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.objects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.client.OptionInstance;

public enum UncappedUnitDouble implements OptionInstance.SliderableValueSet<Double> {
    INSTANCE;

    private static final double minValue = 0.0d;
    private static final double maxValue = 1000.0d;

    @Override
    public double toSliderValue(Double value) {
        double range = maxValue - minValue;
        return (value - minValue) / range;
    }

    @Override
    public Double fromSliderValue(double value) {
        double range = maxValue - minValue;
        return value * range + minValue;
    }

    @Override
    public Optional<Double> validateValue(Double value) {
        return value >= minValue && value <= maxValue ? Optional.of(value) : Optional.empty();
    }

    @Override
    public Codec<Double> codec() {
        return Codec.either(Codec.doubleRange(minValue, maxValue), Codec.BOOL)
                .xmap(either -> either.map(value -> value, bool -> bool ? 1.0d : 0.0d), Either::left);
    }
}
