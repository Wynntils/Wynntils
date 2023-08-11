/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UTILITIES)
public class GammabrightFeature extends Feature {
    @Persisted
    public final Config<Boolean> gammabrightEnabled = new Config<>(false);

    @Persisted
    private final Storage<Double> lastGamma = new Storage<>(1.0);

    @RegisterKeyBind
    private final KeyBind gammabrightKeyBind =
            new KeyBind("Gammabright", GLFW.GLFW_KEY_G, true, this::toggleGammaBright);

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldState.WORLD) return;

        applyGammabright();
    }

    @SubscribeEvent
    public void onDisconnect(WynncraftConnectionEvent.Disconnected event) {
        if (gammabrightEnabled.get()) {
            resetGamma();
        }
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        if (config.getFieldName().equals("gammabrightEnabled")) {
            applyGammabright();
        }
    }

    @Override
    public void onEnable() {
        if (gammabrightEnabled.get() && McUtils.options().gamma().get() != 1000d) {
            enableGammabright();
        }
    }

    @Override
    public void onDisable() {
        resetGamma();
    }

    private void applyGammabright() {
        if (!isEnabled()) return;
        if (gammabrightEnabled.get() && McUtils.options().gamma().get() == 1000d) return;

        if (gammabrightEnabled.get()) {
            enableGammabright();
        } else {
            resetGamma();
        }
    }

    private void toggleGammaBright() {
        gammabrightEnabled.store(!gammabrightEnabled.get());
        applyGammabright();

        gammabrightEnabled.touched();
    }

    private void resetGamma() {
        McUtils.options().gamma().value = lastGamma.get();
    }

    private void enableGammabright() {
        lastGamma.store(McUtils.options().gamma().get());
        McUtils.options().gamma().value = 1000d;
    }
}
