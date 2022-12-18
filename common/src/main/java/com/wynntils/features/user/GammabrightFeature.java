/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.managers.Managers;
import com.wynntils.mc.event.UpdateLightTextureEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.INVARIABLE)
public class GammabrightFeature extends UserFeature {
    @Config
    private boolean gammabrightEnabled = false;

    @RegisterKeyBind
    private final KeyBind gammabrightKeyBind =
            new KeyBind("Gammabright", GLFW.GLFW_KEY_G, true, this::toggleGammaBright);

    @SubscribeEvent
    public void onUpdateLightTexture(UpdateLightTextureEvent event) {
        if (!gammabrightEnabled) return;

        event.setGamma(1000f);
    }

    private void toggleGammaBright() {
        gammabrightEnabled = !gammabrightEnabled;

        Managers.Config.saveConfig();
    }
}
