/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients.type;

public enum IngredientPosition {
    LEFT("to the left of", "left"),
    RIGHT("to the right of", "right"),
    ABOVE("above", "above"),
    UNDER("under", "under"),
    TOUCHING("touching", "touching"),
    NOT_TOUCHING("not touching", "notTouching");

    private final String description;
    private final String apiName;

    IngredientPosition(String description, String apiName) {
        this.description = description;
        this.apiName = apiName;
    }

    public String getDescription() {
        return description;
    }

    public String getApiName() {
        return apiName;
    }
}
