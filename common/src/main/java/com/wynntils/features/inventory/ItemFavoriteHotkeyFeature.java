/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.IngredientItem;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class ItemFavoriteHotkeyFeature extends Feature {
    private static final Pattern PATTERN = Pattern.compile("À$");

    @RegisterKeyBind
    private final KeyBind itemFavoriteKeyBind = new KeyBind(
            "Favorite/Unfavorite Item", GLFW.GLFW_KEY_UNKNOWN, true, null, this::tryChangeFavoriteStateOnHoveredSlot);

    @Persisted
    public final Config<Boolean> allowFavoritingAllItems = new Config<>(false);

    private void tryChangeFavoriteStateOnHoveredSlot(Slot hoveredSlot) {
        if (!Models.WorldState.onWorld() || hoveredSlot == null) return;

        String unformattedName = PATTERN.matcher(
                        StyledText.fromComponent((hoveredSlot.getItem().getHoverName()))
                                .getStringWithoutFormatting())
                .replaceAll("");

        Optional<GearItem> gearItemOpt;
        Optional<IngredientItem> ingredientItemOpt;
        String itemName = null;

        gearItemOpt = Models.Item.asWynnItem(hoveredSlot.getItem(), GearItem.class);
        if (gearItemOpt.isPresent()) itemName = gearItemOpt.get().getName();

        ingredientItemOpt = Models.Item.asWynnItem(hoveredSlot.getItem(), IngredientItem.class);
        if (ingredientItemOpt.isPresent()) itemName = ingredientItemOpt.get().getName();

        if (allowFavoritingAllItems.get()) itemName = unformattedName;

        if (itemName != null && !itemName.isBlank()) Services.Favorites.toggleFavorite(itemName);
    }
}
