/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted;

import com.google.common.base.CaseFormat;
import java.util.Locale;
import net.minecraft.client.resources.language.I18n;

public interface Translatable {
    String getTypeName();

    default String getTranslation(String keySuffix, Object... parameters) {
        return I18n.get(
                getTypeName().toLowerCase(Locale.ROOT) + ".wynntils." + getTranslationKeyName() + "." + keySuffix,
                parameters);
    }

    default String getTranslationKeyName() {
        String name = this.getClass().getSimpleName().replace(getTypeName(), "");
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    default String getTranslatedName() {
        return getTranslation("name");
    }
}
