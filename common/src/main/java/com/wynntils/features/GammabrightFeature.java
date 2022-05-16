/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.FeatureBase;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.INVARIABLE, gameplay = GameplayImpact.LARGE, performance = PerformanceImpact.SMALL)
public class GammabrightFeature extends FeatureBase {
    private double lastGamma = 1f;

    private final KeyHolder gammabrightKeybind =
            new KeyHolder("Gammabright", GLFW.GLFW_KEY_G, "Wynntils", true, this::onGammabrightKeyPress);

    public GammabrightFeature() {
        setupKeyHolder(gammabrightKeybind);
    }

    private void onGammabrightKeyPress() {
        double currentGamma = McUtils.mc().options.gamma;
        if (currentGamma < 1000) {
            lastGamma = currentGamma;
            McUtils.mc().options.gamma = 1000d;
            return;
        }

        McUtils.mc().options.gamma = lastGamma;
    }
}
