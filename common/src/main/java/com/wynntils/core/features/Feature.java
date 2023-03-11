/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.OverlayGroupHolder;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.annotations.OverlayGroup;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.storage.Storageable;
import com.wynntils.utils.mc.McUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 *
 * <p>Ex: Soul Point Timer
 */
public abstract class Feature extends AbstractConfigurable implements Storageable, Translatable, Comparable<Feature> {
    private ImmutableList<Condition> conditions;
    private final List<KeyBind> keyBinds = new ArrayList<>();
    private final Map<Overlay, OverlayInfo> overlays = new LinkedHashMap<>();

    private final List<OverlayGroupHolder> overlayGroups = new ArrayList<>();
    private final List<Overlay> groupedOverlayInstances = new ArrayList<>();

    private FeatureState state = FeatureState.UNINITALIZED;

    private Category category = Category.UNCATEGORIZED;

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
                Managers.Overlay.registerOverlay(overlay, annotation.renderType(), annotation.renderAt(), this);
                overlays.put(overlay, annotation);

                assert !overlay.getTranslatedName().startsWith("feature.wynntils.");
            } catch (IllegalAccessException e) {
                WynntilsMod.error("Unable to get field " + overlayField, e);
            }
        }
    }

    public final void initOverlayGroups() {
        // Unregister old instances
        for (Overlay groupedOverlay : groupedOverlayInstances) {
            Managers.Overlay.unregisterOverlay(groupedOverlay);
        }

        groupedOverlayInstances.clear();

        // Go on with discovering the new ones
        Field[] groupFields = FieldUtils.getFieldsWithAnnotation(this.getClass(), OverlayGroup.class);

        for (Field groupField : groupFields) {
            try {
                Object fieldValue = FieldUtils.readField(groupField, this, true);
                OverlayGroup annotation = groupField.getAnnotation(OverlayGroup.class);

                if (!(fieldValue instanceof List<?> list)) {
                    throw new RuntimeException("A non overlay group field was marked with OverlayGroup annotation.");
                }

                for (Overlay overlay : (List<Overlay>) list) {
                    Managers.Overlay.registerOverlay(overlay, annotation.renderType(), annotation.renderAt(), this);
                    groupedOverlayInstances.add(overlay);

                    assert !overlay.getTranslatedName().startsWith("feature.wynntils.");
                }
            } catch (IllegalAccessException e) {
                WynntilsMod.error("Unable to get field " + groupField, e);
            }
        }
    }

    public final void addOverlayGroups(List<OverlayGroupHolder> groups) {
        overlayGroups.addAll(groups);
    }

    public List<OverlayGroupHolder> getOverlayGroups() {
        return overlayGroups;
    }

    /**
     * Adds a keyBind to the feature. Called from the registry.
     * @param keyBind KeyBind to add to the feature
     */
    public final void setupKeyHolder(KeyBind keyBind) {
        keyBinds.add(keyBind);
    }

    public List<Overlay> getOverlays() {
        return Stream.concat(overlays.keySet().stream(), groupedOverlayInstances.stream())
                .toList();
    }

    public OverlayInfo getOverlayInfo(Overlay overlay) {
        return overlays.get(overlay);
    }

    public final void enableOverlays() {
        Managers.Overlay.enableOverlays(this.getOverlays(), false);
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

    @Override
    public String getStorageJsonName() {
        return "feature." + getNameCamelCase();
    }

    /** Called on init of Feature */
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    /**
     * Called on enabling of Feature
     */
    protected void onEnable() {}

    /** Called on disabling of Feature */
    protected void onDisable() {}

    /** Called after successfully enabling a feature, after everything is set up. */
    protected void postEnable() {}

    /** Called to activate a feature */
    public final void enable() {
        if (state != FeatureState.DISABLED && state != FeatureState.CRASHED) return;

        if (!canEnable()) return;

        onEnable();
        state = FeatureState.ENABLED;

        WynntilsMod.registerEventListener(this);

        enableOverlays();

        for (KeyBind keyBind : keyBinds) {
            Managers.KeyBind.registerKeybind(keyBind);
        }

        // Reload configs to load new keybinds
        if (!keyBinds.isEmpty()) {
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

        WynntilsMod.unregisterEventListener(this);

        Managers.Overlay.disableOverlays(this.getOverlays());
        for (KeyBind keyBind : keyBinds) {
            Managers.KeyBind.unregisterKeybind(keyBind);
        }
    }

    public final void crash() {
        disable();
        state = FeatureState.CRASHED;
    }

    /** Whether a feature is enabled */
    public final boolean isEnabled() {
        return state == FeatureState.ENABLED;
    }

    /** Whether a feature can be enabled */
    private boolean canEnable() {
        for (Condition condition : conditions) {
            if (!condition.isSatisfied()) return false;
        }

        return true;
    }

    /** Used to react to config option updates */
    protected void onConfigUpdate(ConfigHolder configHolder) {}

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
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
        ENABLED,
        CRASHED
    }
}
