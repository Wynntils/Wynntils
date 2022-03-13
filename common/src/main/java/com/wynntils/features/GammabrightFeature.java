/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.mc.utils.keybinds.KeyManager;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.INVARIABLE, gameplay = GameplayImpact.LARGE, performance = PerformanceImpact.SMALL)
public class GammabrightFeature extends Feature {
    private double lastGamma = 1f;

    private final KeyHolder gammabrightKeybind = new KeyHolder("Gammabright", GLFW.GLFW_KEY_G, "Wynntils", true, () -> {
        double currentGamma = McUtils.mc().options.gamma;
        if (currentGamma < 1000) {
            lastGamma = currentGamma;
            McUtils.mc().options.gamma = 1000d;
            return;
        }

        McUtils.mc().options.gamma = lastGamma;
    });

    public String getName() {
        return "Gammabright Keybind Feature";
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    @Override
    protected boolean onEnable() {
        KeyManager.registerKeybind(gammabrightKeybind);
        return true;
    }

    @Override
    protected void onDisable() {
        KeyManager.unregisterKeybind(gammabrightKeybind);
    }
}
