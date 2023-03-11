/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ComparisonChain;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.storage.Storageable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 *
 * <p>Ex: Soul Point Timer
 */
public abstract class Feature extends AbstractConfigurable implements Storageable, Translatable, Comparable<Feature> {
    private final List<KeyBind> keyBinds = new ArrayList<>();

    private Category category = Category.UNCATEGORIZED;

    /**
     * Adds a keyBind to the feature. Called from the registry.
     * @param keyBind KeyBind to add to the feature
     */
    public final void setupKeyHolder(KeyBind keyBind) {
        keyBinds.add(keyBind);
    }

    public List<KeyBind> getKeyBinds() {
        return keyBinds;
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

    /** Whether a feature is enabled */
    public final boolean isEnabled() {
        return Managers.Feature.getFeatureState(this) == FeatureState.ENABLED;
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
}
