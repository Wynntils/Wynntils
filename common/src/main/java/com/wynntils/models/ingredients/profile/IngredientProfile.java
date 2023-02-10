/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients.profile;

import com.google.gson.annotations.SerializedName;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.ingredients.type.IngredientTier;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.SkinUtils;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class IngredientProfile {
    private final String name;

    @SerializedName("tier")
    private final IngredientTier ingredientTier;

    private final boolean untradeable;
    private final int level;
    private final String material;
    private final List<ProfessionType> professions;
    private final Map<String, IngredientIdentificationContainer> statuses;
    private final IngredientItemModifiers itemModifiers;
    private final IngredientModifiers ingredientModifiers;

    @SerializedName("itemInfo")
    private IngredientProfileInfo ingredientInfo;

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

    public IngredientModifiers getIngredientModifiers() {
        return ingredientModifiers;
    }

    public IngredientItemModifiers getItemModifiers() {
        return itemModifiers;
    }

    public boolean isUntradeable() {
        return untradeable;
    }

    public ItemStack asItemStack() {
        ItemStack itemStack = ingredientInfo.asItemStack();

        if (itemStack.getItem() == Items.PLAYER_HEAD) {
            String ingredientHeadTexture = Models.IngredientProfiles.getIngredientHeadTexture(name);
            if (ingredientHeadTexture == null) {
                // This will look bad, but if we don't have the data, then what should we do?
                WynntilsMod.warn("Missing head texture for "
                        + ingredientInfo.asItemStack().getDisplayName());
                return itemStack;
            }
            SkinUtils.setPlayerHeadSkin(itemStack, ingredientHeadTexture);
        }

        return itemStack;
    }

    @Override
    public String toString() {
        return "IngredientProfile{" + "name='"
                + name + '\'' + ", ingredientTier="
                + ingredientTier + ", untradeable="
                + untradeable + ", level="
                + level + ", material='"
                + material + '\'' + ", professions="
                + professions + ", statuses="
                + statuses + ", itemModifiers="
                + itemModifiers + ", ingredientModifiers="
                + ingredientModifiers + ", ingredientInfo="
                + ingredientInfo + '}';
    }
}
