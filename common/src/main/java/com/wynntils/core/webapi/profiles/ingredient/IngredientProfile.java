/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles.ingredient;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;

public class IngredientProfile {
    String name;

    @SerializedName("tier")
    IngredientTier ingredientTier;

    boolean untradeable;
    int level;
    String material;
    List<ProfessionType> professions;
    Map<String, IngredientIdentificationContainer> statuses;
    IngredientItemModifiers itemModifiers;
    IngredientModifiers ingredientModifiers;

    public IngredientProfile(
            String name,
            IngredientTier ingredientTier,
            boolean untradeable,
            int level,
            String material,
            List<ProfessionType> professions,
            Map<String, IngredientIdentificationContainer> statuses,
            IngredientItemModifiers itemModifiers,
            IngredientModifiers ingredientModifiers) {
        this.name = name;
        this.ingredientTier = ingredientTier;
        this.untradeable = untradeable;
        this.level = level;
        this.material = material;
        this.professions = professions;
        this.statuses = statuses;
        this.itemModifiers = itemModifiers;
        this.ingredientModifiers = ingredientModifiers;
    }

    public String getIngredientStringFormatted() {
        return ChatFormatting.GRAY + name + " " + ingredientTier.getTierString();
    }

    public String getDisplayName() {
        return name;
    }

    public List<ProfessionType> getProfessions() {
        return professions;
    }

    public int getLevel() {
        return level;
    }

    public IngredientTier getTier() {
        return ingredientTier;
    }

    public Map<String, IngredientIdentificationContainer> getStatuses() {
        return this.statuses;
    }
}
