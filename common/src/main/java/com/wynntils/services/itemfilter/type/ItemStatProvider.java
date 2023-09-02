/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.google.common.base.CaseFormat;
import com.wynntils.core.persisted.Translatable;
import com.wynntils.models.items.WynnItem;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import net.minecraft.client.resources.language.I18n;

public abstract class ItemStatProvider<T> implements Translatable {
    protected final String name;

    protected ItemStatProvider() {
        String name = this.getClass().getSimpleName().replace("StatProvider", "");
        this.name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    /**
     * Returns the value of the stat for the given item.
     * If there is a single value, it is returned as a singleton list.
     * Some stats may have multiple values, in which case a list is returned.
     * @param wynnItem The item to get the stat value for
     * @return The value of the stat for the given item
     */
    public abstract List<T> getValue(WynnItem wynnItem);

    public final Class<T> getType() {
        return (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public List<String> getAliases() {
        return List.of();
    }

    public String getName() {
        return name;
    }

    protected String getTranslationKey() {
        return name;
    }

    @Override
    public String getTranslation(String keySuffix, Object... formatValues) {
        return I18n.get("service.wynntils.itemFilter.stat." + getTranslationKey() + "." + keySuffix, formatValues);
    }

    @Override
    public String getTypeName() {
        return "Service";
    }

    public String getDescription() {
        return getTranslation("description");
    }
}
