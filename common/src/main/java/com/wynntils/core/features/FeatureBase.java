/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.mc.utils.keybinds.KeyManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class FeatureBase extends Feature {
    private boolean isListener = false;
    private List<KeyHolder> keyMappings = new ArrayList<>();

    /**
     * Sets up this feature as an event listener. This should be called from the constructor.
     * Behaviour if calling it after the feature is enabled is undefined.
     */
    public void setupEventListener() {
        this.isListener = true;
    }

    /**
     * Adds a keyHolder to the feature. This should be called from the constructor.
     * Behaviour if calling it after the feature is enabled is undefined.
     * @param keyHolder KeyHolder to add to the feature
     */
    public void setupKeyHolder(KeyHolder keyHolder) {
        keyMappings.add(keyHolder);
    }

    protected String getNameCamelCase() {
        String name = this.getClass().getTypeName().replace("Feature", "");
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils." + getNameCamelCase() + ".name");
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    @Override
    protected boolean onEnable() {
        if (isListener) {
            WynntilsMod.getEventBus().register(this);
        }
        for (KeyHolder key : keyMappings) {
            KeyManager.registerKeybind(key);
        }
        return true;
    }

    @Override
    protected void onDisable() {
        if (isListener) {
            WynntilsMod.getEventBus().unregister(this);
        }
        for (KeyHolder key : keyMappings) {
            KeyManager.unregisterKeybind(key);
        }
    }
}
