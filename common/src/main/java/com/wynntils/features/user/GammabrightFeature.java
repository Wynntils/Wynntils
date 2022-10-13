/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.INVARIABLE)
public class GammabrightFeature extends UserFeature {
    @Config
    private boolean gammabrightEnabled = false;

    private double lastGamma = 1f;

    @RegisterKeyBind
    private final KeyBind gammabrightKeyBind =
            new KeyBind("Gammabright", GLFW.GLFW_KEY_G, true, this::onGammabrightKeyPress);

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldStateManager.State.WORLD) return;
        if (!gammabrightEnabled) return;

        lastGamma = McUtils.options().gamma;
        McUtils.options().gamma = 1000d;
    }

    private void onGammabrightKeyPress() {
        double currentGamma = McUtils.options().gamma;
        if (currentGamma < 1000) {
            lastGamma = currentGamma;
            McUtils.options().gamma = 1000d;
            gammabrightEnabled = true;
        } else {
            gammabrightEnabled = false;
            McUtils.options().gamma = lastGamma;
        }

        ConfigManager.saveConfig();
    }
}
