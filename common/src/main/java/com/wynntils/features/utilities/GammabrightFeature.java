/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.DimensionAmbientLightEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UTILITIES)
public class GammabrightFeature extends Feature {
    @Persisted
    private final Config<Boolean> gammabrightEnabled = new Config<>(false);

    @RegisterKeyBind
    private final KeyBind gammabrightKeyBind =
            new KeyBind("Gammabright", GLFW.GLFW_KEY_G, true, this::toggleGammaBright);

    @SubscribeEvent
    public void onGetDimensionAmbientLight(DimensionAmbientLightEvent event) {
        event.setCanceled(gammabrightEnabled.get());
    }

    private void toggleGammaBright() {
        gammabrightEnabled.store(!gammabrightEnabled.get());
        gammabrightEnabled.touched();
    }
}
