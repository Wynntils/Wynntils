/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.google.common.base.CaseFormat;
import com.wynntils.core.persisted.Translatable;
import java.util.Optional;
import net.minecraft.client.resources.language.I18n;

/**
 * A factory for creating {@link StatFilter} instances.
 * @param <T>
 */
public abstract class StatFilterFactory<T> implements Translatable {
    protected final String name;
    protected final String translationKey;

    protected StatFilterFactory() {
        String name = this.getClass().getSimpleName().replace("StatFilterFactory", "");
        this.name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        this.translationKey = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    public abstract Optional<T> create(String inputString);

    public String getName() {
        return name;
    }

    protected String getTranslationKey() {
        return translationKey;
    }

    @Override
    public String getTranslation(String keySuffix, Object... formatValues) {
        return I18n.get("service.wynntils.itemFilter.filter." + getTranslationKey() + "." + keySuffix, formatValues);
    }

    @Override
    public String getTypeName() {
        return "Service";
    }

    public String getDescription() {
        return getTranslation("description");
    }

    public String getUsage() {
        return getTranslation("usage");
    }
}
