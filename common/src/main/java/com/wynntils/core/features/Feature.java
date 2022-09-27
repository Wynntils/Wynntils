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
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.event.WebSetupEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 *
 * <p>Ex: Soul Point Timer
 */
public abstract class Feature implements Translatable, Configurable, Comparable<Feature>, ModelDependant {
    private ImmutableList<Condition> conditions;
    private boolean isListener = false;
    private final List<KeyBind> keyBinds = new ArrayList<>();
    private final List<ConfigHolder> configOptions = new ArrayList<>();
    private final List<Overlay> overlays = new ArrayList<>();

    protected boolean enabled = false;

    protected boolean initFinished = false;

    private FeatureCategory category = FeatureCategory.UNCATEGORIZED;

    public final void init() {
        ImmutableList.Builder<Condition> conditions = new ImmutableList.Builder<>();

        onInit(conditions);

        this.conditions = conditions.build();

        if (!this.conditions.isEmpty()) this.conditions.forEach(Condition::init);

        initFinished = true;

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

    protected String getNameCamelCase() {
        String name = this.getClass().getSimpleName().replace("Feature", "");
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
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

        List<Class<? extends Model>> dependencies = getModelDependencies();

        for (Class<? extends Model> dependency : dependencies) {
            ManagerRegistry.addDependency(this, dependency);
        }

        if (isListener) {
            WynntilsMod.registerEventListener(this);
        }
        OverlayManager.enableOverlays(this.overlays, false);
        for (KeyBind keyBind : keyBinds) {
            KeyBindManager.registerKeybind(keyBind);
        }
    }

    /** Called for a feature's deactivation */
    public final void disable() {
        if (!enabled) throw new IllegalStateException("Feature can not be disabled as it already is disabled");

        onDisable();

        enabled = false;

        ManagerRegistry.removeAllDependencies(this);

        if (isListener) {
            WynntilsMod.unregisterEventListener(this);
        }
        OverlayManager.disableOverlays(this.overlays);
        for (KeyBind keyBind : keyBinds) {
            KeyBindManager.unregisterKeybind(keyBind);
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

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of();
    }

    public boolean canUserEnable() {
        return this instanceof UserFeature;
    }

    /** Registers the feature's config options. Called by ConfigManager when feature is loaded */
    @Override
    public final void addConfigOptions(List<ConfigHolder> options) {
        configOptions.addAll(options);
    }

    /** Returns all config options registered in this feature */
    public final List<ConfigHolder> getConfigOptions() {
        return configOptions;
    }

    /** Returns all config options registered in this feature that should be visible to the user */
    public final List<ConfigHolder> getVisibleConfigOptions() {
        return configOptions.stream().filter(c -> c.getMetadata().visible()).collect(Collectors.toList());
    }

    /** Returns the config option matching the given name, if it exists */
    public final Optional<ConfigHolder> getConfigOptionFromString(String name) {
        return getVisibleConfigOptions().stream()
                .filter(c -> c.getFieldName().equals(name))
                .findFirst();
    }

    /** Used to react to config option updates */
    protected void onConfigUpdate(ConfigHolder configHolder) {}

    @Override
    public String getConfigJsonName() {
        String name = this.getClass().getSimpleName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

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

    public static class WebLoadedCondition extends Condition {
        @Override
        public void init() {
            if (WebManager.isSetup()) {
                setSatisfied(true);
                return;
            }

            WynntilsMod.registerEventListener(this);
        }

        @SubscribeEvent
        public void onWebSetup(WebSetupEvent e) {
            setSatisfied(true);
            WynntilsMod.unregisterEventListener(this);
        }

        @Override
        public boolean isSatisfied() {
            return super.isSatisfied() || WebManager.isSetup();
        }
    }

    public abstract static class Condition {
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
