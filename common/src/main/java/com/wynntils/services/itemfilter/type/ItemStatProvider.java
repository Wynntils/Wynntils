/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.google.common.base.CaseFormat;
import com.wynntils.core.persisted.Translatable;
import com.wynntils.models.items.WynnItem;
import java.lang.reflect.ParameterizedType;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.resources.language.I18n;

public abstract class ItemStatProvider<T extends Comparable<T>> implements Translatable, Comparator<WynnItem> {
    private final String name;

    protected ItemStatProvider() {
        String name = this.getClass().getSimpleName().replace("StatProvider", "");
        this.name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    /**
     * Returns the value of the stat for the given item, as an optional.
     *
     * @param wynnItem The item to get the stat value for
     * @return The value of the stat for the given item
     */
    public abstract Optional<T> getValue(WynnItem wynnItem);

    public List<String> getValidInputs() {
        return List.of();
    }

    /**
     * Returns the type of filter this stat provider is for.
     * @return The type of filter this stat provider is for
     */
    public abstract List<ItemProviderType> getFilterTypes();

    public Class<T> getType() {
        return (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public List<String> getAliases() {
        return List.of();
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return getTranslation("name");
    }

    @Override
    public String getTranslation(String keySuffix, Object... formatValues) {
        return I18n.get("service.wynntils.itemFilter.stat." + name + "." + keySuffix, formatValues);
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
        Optional<T> itemValue1 = this.getValue(wynnItem1);
        Optional<T> itemValue2 = this.getValue(wynnItem2);

        if (itemValue1.isEmpty() && itemValue2.isPresent()) return 1;
        if (itemValue1.isPresent() && itemValue2.isEmpty()) return -1;
        if (itemValue1.isEmpty() && itemValue2.isEmpty()) return 0;

        return -itemValue1.get().compareTo(itemValue2.get());
    }
}
