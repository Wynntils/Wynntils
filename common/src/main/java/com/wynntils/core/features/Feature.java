/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Config;
import com.wynntils.core.keybinds.KeyHolder;
import com.wynntils.core.keybinds.KeyManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.event.WebSetupEvent;
import com.wynntils.mc.utils.ComponentUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 *
 * <p>Ex: Soul Point Timer
 */
public abstract class Feature {
    private ImmutableList<Condition> conditions;
    private boolean isListener = false;
    private List<KeyHolder> keyMappings = new ArrayList<>();

    protected boolean enabled = false;

    public final void init() {
        ImmutableList.Builder<Condition> conditions = new ImmutableList.Builder<>();

        onInit(conditions);

        this.conditions = conditions.build();

        if (this.conditions.isEmpty()) return;
        this.conditions.forEach(Condition::init);
    }

    /**
     * Sets up this feature as an event listener. Called from the registry.
     */
    public void setupEventListener() {
        this.isListener = true;
    }

    /**
     * Adds a keyHolder to the feature. Called from the registry.
     * @param keyHolder KeyHolder to add to the feature
     */
    public void setupKeyHolder(KeyHolder keyHolder) {
        keyMappings.add(keyHolder);
    }

    /** Gets the name of a feature */
    public String getTranslatedName() {
        return ComponentUtils.getFormatted(getNameComponent());
    }

    public String getShortName() {
        String typeName = this.getClass().getTypeName();
        return typeName.substring(typeName.lastIndexOf('.') + 1);
    }

    protected String getNameCamelCase() {
        String name = this.getClass().getTypeName().replace("Feature", "");
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils." + getNameCamelCase() + ".name");
    }

    /** Called on init of Feature */
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    /**
     * Called on enabling of Feature
     *
     * <p>Return false to cancel enabling, return true to continue. Note that if a feature's enable
     * is cancelled it isn't called again by the conditions and must be done so manually, likely by
     * the user.
     */
    protected boolean onEnable() {
        return true;
    }

    /** Called on disabling of Feature */
    protected void onDisable() {}

    /** Called to activate a feature */
    public final void enable() {
        if (enabled) throw new IllegalStateException("Feature can not be enabled as it already is enabled");

        if (!canEnable()) return;
        if (!onEnable()) return;

        enabled = true;

        if (isListener) {
            WynntilsMod.getEventBus().register(this);
        }
        for (KeyHolder key : keyMappings) {
            KeyManager.registerKeybind(key);
        }
    }

    /** Called for a feature's deactivation */
    public final void disable() {
        if (!enabled) throw new IllegalStateException("Feature can not be disabled as it already is disabled");

        onDisable();

        enabled = false;

        if (isListener) {
            WynntilsMod.getEventBus().unregister(this);
        }
        for (KeyHolder key : keyMappings) {
            KeyManager.unregisterKeybind(key);
        }
    }

    public final void tryEnable() {
        if (enabled) return;

        enable();
    }

    public final void tryDisable() {
        if (!enabled) return;

        disable();
    }

    /** Whether a feature is enabled */
    public final boolean isEnabled() {
        return enabled;
    }

    /** Whether a feature can be enabled */
    public boolean canEnable() {
        for (Condition condition : conditions) {
            if (!condition.isSatisfied()) return false;
        }

        return true;
    }

    public final Field[] getConfigFields() {
        return FieldUtils.getFieldsWithAnnotation(this.getClass(), Config.class);
    }

    public class WebLoadedCondition extends Condition {
        @Override
        public void init() {
            if (WebManager.isSetup()) {
                setSatisfied(true);
                return;
            }

            WynntilsMod.getEventBus().register(this);
        }

        @SubscribeEvent
        public void onWebSetup(WebSetupEvent e) {
            setSatisfied(true);
            WynntilsMod.getEventBus().unregister(this);
        }
    }

    public abstract class Condition {
        boolean satisfied = false;

        public boolean isSatisfied() {
            return satisfied;
        }

        public abstract void init();

        public void setSatisfied(boolean satisfied) {
            this.satisfied = satisfied;
        }
    }
}
