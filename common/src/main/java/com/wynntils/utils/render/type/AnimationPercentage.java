/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.type;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import org.joml.Math;

public final class AnimationPercentage {
    private final Supplier<Boolean> openingConditions;
    private Duration openingDuration;
    private Instant lastTime = Instant.EPOCH;

    private double openingProgress;

    public AnimationPercentage(Supplier<Boolean> openingConditions, Duration openingDuration) {
        this.openingConditions = openingConditions;
        this.openingDuration = openingDuration;
    }

    public double getAnimation() {
        if (openingDuration.isZero() || openingDuration.isNegative()) return 1;
        if (openingProgress == 0) {
            lastTime = Instant.now().minus(1, ChronoUnit.MILLIS);
        }

        if (openingConditions.get()) {
            addOpeningProgress(
                    Duration.between(lastTime, Instant.now()).toMillis() / ((double) openingDuration.toMillis()));
        } else if (openingProgress > 0) {
            addOpeningProgress(
                    -Duration.between(lastTime, Instant.now()).toMillis() / ((double) openingDuration.toMillis()));
        }
        lastTime = Instant.now();
        return applyTransformation(openingProgress);
    }

    public void addOpeningProgress(double openingProgress) {
        setOpeningProgress(this.openingProgress + openingProgress);
    }

    public void setOpeningProgress(double openingProgress) {
        this.openingProgress = Math.clamp(0, 1, openingProgress);
    }

    private static double applyTransformation(double openingProgress) {
        return java.lang.Math.sin((float) (openingProgress / 2f * java.lang.Math.PI));
    }

    public void setOpeningDuration(Duration openingDuration) {
        this.openingDuration = openingDuration;
    }

    public boolean finishedClosingAnimation() {
        return openingProgress == 0;
    }
}
