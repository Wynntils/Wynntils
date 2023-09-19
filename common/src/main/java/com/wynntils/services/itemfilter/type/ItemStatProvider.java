/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.google.common.base.CaseFormat;
import com.wynntils.core.persisted.Translatable;
import com.wynntils.models.items.WynnItem;
import java.lang.reflect.ParameterizedType;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.resources.language.I18n;

public abstract class ItemStatProvider<T extends Comparable<T>> implements Translatable, Comparator<WynnItem> {
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

    public Class<T> getType() {
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

    @Override
    public int compare(WynnItem wynnItem1, WynnItem wynnItem2) {
        List<T> itemValues1 = this.getValue(wynnItem1);
        List<T> itemValues2 = this.getValue(wynnItem2);

        if (itemValues1.isEmpty() && !itemValues2.isEmpty()) return 1;
        if (!itemValues1.isEmpty() && itemValues2.isEmpty()) return -1;
        if (itemValues1.isEmpty() && itemValues2.isEmpty()) return 0;

        return -itemValues1.get(0).compareTo(itemValues2.get(0));
    }
}
