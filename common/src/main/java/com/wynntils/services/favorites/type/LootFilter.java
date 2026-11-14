/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.favorites.type;

import java.util.ArrayList;
import java.util.List;

public final class LootFilter {
    public static final String DEFAULT_NAME = "Default";

    private String name;
    private List<FavoriteCategory> categories = new ArrayList<>();

    // Used by Gson
    private LootFilter() {}

    public LootFilter(String name) {
        this.name = name;
        this.categories.add(new FavoriteCategory(FavoriteCategory.DEFAULT_NAME));
    }

    public LootFilter copy() {
        LootFilter copy = new LootFilter();
        copy.name = name;
        categories.forEach(category -> copy.categories.add(category.copy()));
        return copy;
    }

    public boolean isValid() {
        return name != null
                && !name.isBlank()
                && categories != null
                && !categories.isEmpty()
                && categories.stream().allMatch(category -> category != null && category.isValid());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FavoriteCategory> getCategories() {
        return categories;
    }
}
