/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

import com.google.common.collect.ComparisonChain;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.persisted.storage.Storageable;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 */
public abstract class Feature extends AbstractConfigurable implements Storageable, Comparable<Feature> {
    private Category category = Category.UNCATEGORIZED;

    @Persisted(i18nKey = "feature.wynntils.userFeature.userEnabled")
    public final Config<Boolean> userEnabled = new Config<>(true);

    protected Feature(ProfileDefault profileDefault) {
        for (ConfigProfile profile : ConfigProfile.values()) {
            boolean enabled = profileDefault.getDefault(profile);
            this.userEnabled.withDefault(profile, enabled);
        }
    }

    @Override
    public String getTypeName() {
        return "Feature";
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getTranslatedDescription() {
        return getTranslation("description");
    }

    public String getShortName() {
        return this.getClass().getSimpleName().replace("Feature", "");
    }

    @Override
    public String getStorageJsonName() {
        return "feature." + getTranslationKeyName();
    }

    /** Used to react to config option updates */
    protected void onConfigUpdate(Config<?> config) {}

    private void callOnConfigUpdate(Config<?> config) {
        try {
            onConfigUpdate(config);
        } catch (Throwable t) {
            // We can't stop disabled features from getting config updates, so if it crashes again,
            // just ignore it
            if (!Managers.Feature.isEnabled(this)) return;

            Managers.Feature.crashFeature(this);
            WynntilsMod.reportCrash(
                    CrashType.FEATURE, getTranslatedName(), getClass().getName(), "config update", t);
        }
    }

    public void onEnable() {}

    public void onDisable() {}

    /** Whether a feature is enabled */
    public final boolean isEnabled() {
        return Managers.Feature.isEnabled(this);
    }

    public void setUserEnabled(boolean newState) {
        this.userEnabled.store(newState);
        tryUserToggle();
    }

    @Override
    public final void updateConfigOption(Config<?> config) {
        // if user toggle was changed, enable/disable feature accordingly
        if (config.getFieldName().equals("userEnabled")) {
            // Toggling before init does not do anything, so we don't worry about it for now
            tryUserToggle();
            return;
        }

        // otherwise, trigger regular config update
        callOnConfigUpdate(config);
    }

    /** Updates the feature's enabled/disabled state to match the user's setting, if necessary */
    private void tryUserToggle() {
        if (userEnabled.get()) {
            Managers.Feature.enableFeature(this);
        } else {
            Managers.Feature.disableFeature(this, false);
        }
    }

    @Override
    public int compareTo(Feature other) {
        return ComparisonChain.start()
                .compare(this.getCategory().toString(), other.getCategory().toString())
                .compare(this.getTranslatedName(), other.getTranslatedName())
                .result();
    }
}
