/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.google.common.base.CaseFormat;
import com.wynntils.utils.type.ErrorOr;
import java.util.List;
import net.minecraft.client.resources.language.I18n;

/**
 * An {@link ItemFilter} is both : <br>
 * - A model to represent a filter, holding its metadata (name, aliases, etc.)<br>
 * - A factory that will check the validity of an input string for this filter and create an
 *   {@link ItemFilterInstance} from it.<br>
 * <br>
 * The actual logic of the filter (i.e. the code that will check if an item matches the filter) is
 * implemented in the {@link ItemFilterInstance} returned by {@link #createInstance(String)}.
 */
public abstract class ItemFilter {
    protected final String name;
    protected final String translationName;

    protected ItemFilter() {
        String name = this.getClass().getSimpleName().replace("ItemFilter", "");
        this.name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        this.translationName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    /**
     * Creates a new item filter matcher from the given input string.
     *
     * @param inputString the input string
     * @return the created item filter, or a translated error string if the input string is invalid
     */
    public abstract ErrorOr<? extends ItemFilterInstance> createInstance(String inputString);

    public abstract List<String> getAliases();

    public String getName() {
        return name;
    }

    protected String getTranslationName() {
        return translationName;
    }

    protected String getTranslation(String keySuffix, Object... formatValues) {
        return I18n.get("service.wynntils.itemFilter." + getTranslationName() + "." + keySuffix, formatValues);
    }

    public String getUsage() {
        return getTranslation("usage");
    }

    public String getTranslatedName() {
        return getTranslation("name");
    }

    public String getDescription() {
        return getTranslation("description");
    }
}
