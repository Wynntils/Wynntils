/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.DimensionAmbientLightEvent;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class GammabrightFeature extends Feature {
    @Persisted
    private final Config<Boolean> gammabrightEnabled = new Config<>(false);

    @RegisterKeyBind
    private final KeyBind gammabrightKeyBind = KeyBindDefinition.TOGGLE_GAMMABRIGHT.create(this::toggleGammaBright);

    public GammabrightFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }

    @SubscribeEvent
    public void onGetDimensionAmbientLight(DimensionAmbientLightEvent event) {
        event.setCanceled(gammabrightEnabled.get());
    }

    private void toggleGammaBright() {
        gammabrightEnabled.store(!gammabrightEnabled.get());
        gammabrightEnabled.touched();
    }
}
