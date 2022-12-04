/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.managers.ManagerRegistry;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.utils.McUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 *
 * <p>Ex: Soul Point Timer
 */
public abstract class Feature extends AbstractConfigurable
        implements Translatable, Comparable<Feature>, ModelDependant {
    private ImmutableList<Condition> conditions;
    private boolean isListener = false;
    private final List<KeyBind> keyBinds = new ArrayList<>();
    private final List<Overlay> overlays = new ArrayList<>();

    protected FeatureState state = FeatureState.UNINITALIZED;

    private FeatureCategory category = FeatureCategory.UNCATEGORIZED;

    public final void init() {
        ImmutableList.Builder<Condition> conditions = new ImmutableList.Builder<>();

        onInit(conditions);

        this.conditions = conditions.build();

        if (!this.conditions.isEmpty()) this.conditions.forEach(Condition::init);

        state = FeatureState.DISABLED;

        assert !getTranslatedName().startsWith("feature.wynntils.");
    }

    public final void initOverlays() {
        Field[] overlayFields = FieldUtils.getFieldsWithAnnotation(this.getClass(), OverlayInfo.class);
        for (Field overlayField : overlayFields) {

            try {
                Object fieldValue = FieldUtils.readField(overlayField, this, true);

                if (!(fieldValue instanceof Overlay overlay)) {
                    throw new RuntimeException("A non-Overlay class was marked with OverlayInfo annotation.");
                }

                OverlayInfo annotation = overlayField.getAnnotation(OverlayInfo.class);
                OverlayManager.registerOverlay(overlay, annotation, this);
                overlays.add(overlay);

                assert !overlay.getTranslatedName().startsWith("feature.wynntils.");
            } catch (IllegalAccessException e) {
                WynntilsMod.error("Unable to get field " + overlayField, e);
            }
        }
    }

    /**
     * Sets up this feature as an event listener. Called from the registry.
     */
    public final void setupEventListener() {
        this.isListener = true;
    }

    /**
     * Adds a keyBind to the feature. Called from the registry.
     * @param keyBind KeyBind to add to the feature
     */
    public final void setupKeyHolder(KeyBind keyBind) {
        keyBinds.add(keyBind);
    }

    public List<Overlay> getOverlays() {
        return overlays;
    }

    /** Gets the name of a feature */
    @Override
    public String getTranslatedName() {
        return getTranslation("name");
    }

    @Override
    public String getTranslation(String keySuffix) {
        return I18n.get("feature.wynntils." + getNameCamelCase() + "." + keySuffix);
    }

    public String getShortName() {
        return this.getClass().getSimpleName();
    }

    private String getNameCamelCase() {
        String name = this.getClass().getSimpleName().replace("Feature", "");
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    /** Called on init of Feature */
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    /**
     * Called on enabling of Feature
     */
    protected boolean onEnable() {
        return true;
    }

    /** Called on disabling of Feature */
    protected void onDisable() {}

    /** Called after successfully enabling a feature, after everything is set up. */
    protected void postEnable() {}

    /** Called to activate a feature */
    public final void enable() {
        if (state != FeatureState.DISABLED) return;

        if (!canEnable()) return;

        onEnable();
        state = FeatureState.ENABLED;

        ManagerRegistry.addAllDependencies(this);

        if (isListener) {
            WynntilsMod.registerEventListener(this);
        }
        OverlayManager.enableOverlays(this.overlays, false);
        for (KeyBind keyBind : keyBinds) {
            KeyBindManager.registerKeybind(keyBind);
        }

        // Reload configs to load new keybinds
        if (!keyBinds.isEmpty() && FeatureRegistry.isInitCompleted()) {
            synchronized (McUtils.options()) {
                McUtils.mc().options.load();
            }
        }

        postEnable();
    }

    /** Called for a feature's deactivation */
    public final void disable() {
        if (state != FeatureState.ENABLED) return;

        onDisable();

        state = FeatureState.DISABLED;

        ManagerRegistry.removeAllDependencies(this);

        if (isListener) {
            WynntilsMod.unregisterEventListener(this);
        }
        OverlayManager.disableOverlays(this.overlays);
        for (KeyBind keyBind : keyBinds) {
            KeyBindManager.unregisterKeybind(keyBind);
        }
    }

    /** Whether a feature is enabled */
    public final boolean isEnabled() {
        return state == FeatureState.ENABLED;
    }

    /** Whether a feature can be enabled */
    public boolean canEnable() {
        for (Condition condition : conditions) {
            if (!condition.isSatisfied()) return false;
        }

        return true;
    }

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of();
    }

    /** Used to react to config option updates */
    protected void onConfigUpdate(ConfigHolder configHolder) {}

    public FeatureCategory getCategory() {
        return category;
    }

    public void setCategory(FeatureCategory category) {
        this.category = category;
    }

    @Override
    public int compareTo(Feature other) {
        return ComparisonChain.start()
                .compare(this.getCategory().toString(), other.getCategory().toString())
                .compare(this.getTranslatedName(), other.getTranslatedName())
                .result();
    }

    public abstract static class Condition {
        private boolean satisfied = false;

        protected boolean isSatisfied() {
            return satisfied;
        }

        public abstract void init();

        public void setSatisfied(boolean satisfied) {
            this.satisfied = satisfied;
        }
    }

    public enum FeatureState {
        UNINITALIZED,
        DISABLED,
        ENABLED;
    }
}
