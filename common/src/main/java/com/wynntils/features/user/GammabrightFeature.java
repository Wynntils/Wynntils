/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.INVARIABLE)
public class GammabrightFeature extends UserFeature {
    @Config
    private boolean gammabrightEnabled = false;

    @Config(visible = false)
    private double lastGamma = 1f;

    @RegisterKeyBind
    private final KeyBind gammabrightKeyBind =
            new KeyBind("Gammabright", GLFW.GLFW_KEY_G, true, this::toggleGammaBright);

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldState.WORLD) return;

        applyGammabright();
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        if (configHolder.getFieldName().equals("gammabrightEnabled")) {
            applyGammabright();
        }
    }

    @Override
    protected void onDisable() {
        resetGamma();
    }

    @Override
    protected boolean onEnable() {
        if (gammabrightEnabled && McUtils.options().gamma().get() != 1000d) {
            enableGammabright();
        }

        return true;
    }

    private void applyGammabright() {
        if (!isEnabled()) return;
        if (gammabrightEnabled && McUtils.options().gamma().get() == 1000d) return;

        if (gammabrightEnabled) {
            enableGammabright();
        } else {
            resetGamma();
        }
    }

    private void toggleGammaBright() {
        gammabrightEnabled = !gammabrightEnabled;
        applyGammabright();

        Managers.Config.saveConfig();
    }

    private void resetGamma() {
        McUtils.options().gamma().value = lastGamma;
    }

    private void enableGammabright() {
        lastGamma = McUtils.options().gamma().get();
        McUtils.options().gamma().value = 1000d;
    }
}
