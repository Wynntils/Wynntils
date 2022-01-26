/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import org.lwjgl.glfw.GLFW;

public class GammabrightFeature extends Feature {
    private double lastGamma = 1f;

    {
        keybinds.add(
                () ->
                        new KeyHolder(
                                "Gammabright",
                                GLFW.GLFW_KEY_G,
                                "Wynntils",
                                true,
                                () -> {
                                    double currentGamma = McUtils.mc().options.gamma;
                                    if (currentGamma < 1000) {
                                        lastGamma = currentGamma;
                                        McUtils.mc().options.gamma = 1000d;
                                        return;
                                    }

                                    McUtils.mc().options.gamma = lastGamma;
                                }));
    }

    @Override
    public PerformanceImpact getPerformanceImpact() {
        return PerformanceImpact.SMALL;
    }

    @Override
    public GameplayImpact getGameplayImpact() {
        return GameplayImpact.LARGE;
    }

    @Override
    public Stability getStability() {
        return Stability.INVARIABLE;
    }
}
