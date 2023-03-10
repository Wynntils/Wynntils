/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.google.common.base.CaseFormat;
import com.wynntils.core.config.AbstractConfigurable;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.storage.Storageable;
import java.util.Locale;
import net.minecraft.client.resources.language.I18n;

public abstract class CoreComponent extends AbstractConfigurable implements Storageable, Translatable {
    @Override
    public String getStorageJsonName() {
        String name = this.getClass().getSimpleName().replace(getComponentType(), "");
        String nameCamelCase = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
        return getComponentType().toLowerCase(Locale.ROOT) + "." + nameCamelCase;
    }

    @Override
    public void updateConfigOption(ConfigHolder configHolder) {
        // To keep consistency with Features/Overlays, delegate
        // this call
        onConfigUpdate(configHolder);
    }

    protected void onConfigUpdate(ConfigHolder configHolder) {
        // By default, ignore config updates
    }

    @Override
    public String getTranslatedName() {
        return getTranslation("name");
    }

    @Override
    public String getTranslation(String keySuffix) {
        String name = this.getShortName().replace(getComponentType(), "");
        String nameCamelCase = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);

        return I18n.get(getComponentType().toLowerCase(Locale.ROOT) + ".wynntils." + nameCamelCase + "." + keySuffix);
    }

    protected abstract String getComponentType();
}
