/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ComparisonChain;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.storage.Storageable;
import net.minecraft.client.resources.language.I18n;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 */
public abstract class Feature extends AbstractConfigurable implements Storageable, Translatable, Comparable<Feature> {
    private Category category = Category.UNCATEGORIZED;

    @RegisterConfig(i18nKey = "feature.wynntils.userFeature.userEnabled")
    public final Config<Boolean> userEnabled = new Config<>(true);

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    /** Gets the name of a feature */
    @Override
    public String getTranslatedName() {
        return getTranslation("name");
    }

    public String getTranslatedDescription() {
        return getTranslation("description");
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

    /** Used to react to config option updates */
    protected void onConfigUpdate(ConfigHolder<?> configHolder) {}

    public void onEnable() {}

    public void onDisable() {}

    /** Whether a feature is enabled */
    public final boolean isEnabled() {
        return Managers.Feature.isEnabled(this);
    }

    public void setUserEnabled(boolean newState) {
        this.userEnabled.updateConfig(newState);
        tryUserToggle();
    }

    @Override
    public final void updateConfigOption(ConfigHolder<?> configHolder) {
        // if user toggle was changed, enable/disable feature accordingly
        if (configHolder.getFieldName().equals("userEnabled")) {
            // Toggling before init does not do anything, so we don't worry about it for now
            tryUserToggle();
            return;
        }

        // otherwise, trigger regular config update
        onConfigUpdate(configHolder);
    }

    /** Updates the feature's enabled/disabled state to match the user's setting, if necessary */
    private void tryUserToggle() {
        if (userEnabled.get()) {
            Managers.Feature.enableFeature(this);
        } else {
            Managers.Feature.disableFeature(this);
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
