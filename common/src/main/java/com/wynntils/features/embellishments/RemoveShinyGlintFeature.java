/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.embellishments;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.DataComponentGetEvent;
import com.wynntils.models.items.properties.ShinyItemProperty;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.EMBELLISHMENTS)
public class RemoveShinyGlintFeature extends Feature {
    // If this changes, add it to Static-Storage model_data so we don't need to push mod updates to fix
    private static final int SHINY_GLINT_COLOR = 2096896;

    @Persisted
    private final Config<Boolean> replaceGlint = new Config<>(true);

    // Weapons use potion color
    @SubscribeEvent
    public void onGetPotionContents(DataComponentGetEvent.PotionContents event) {
        if (!hasShinyStat(event.getItemStack())) return;

        PotionContents itemStackPotionContents = event.getOriginalValue();

        Optional<Integer> potionColor = itemStackPotionContents.customColor();

        if (potionColor.isEmpty()) return;
        if (potionColor.get() != SHINY_GLINT_COLOR) return;

        event.setValue(new PotionContents(
                itemStackPotionContents.potion(),
                Optional.empty(),
                itemStackPotionContents.customEffects(),
                itemStackPotionContents.customName()));
    }

    // Armor uses dye color
    @SubscribeEvent
    public void onGetDyeColor(DataComponentGetEvent.DyedItemColor event) {
        if (!hasShinyStat(event.getItemStack())) return;

        DyedItemColor dyeColor = event.getOriginalValue();

        if (dyeColor == null) return;
        if (dyeColor.rgb() != SHINY_GLINT_COLOR) return;

        event.setValue(null);
    }

    @SubscribeEvent
    public void onGetEnchantmentOverride(DataComponentGetEvent.EnchantmentGlintOverride event) {
        if (!hasShinyStat(event.getItemStack())) return;

        // Give it the enchanted effect similar to how shinies were displayed prior to the introduction of glints
        if (replaceGlint.get()) {
            event.setValue(true);
        }
    }

    private boolean hasShinyStat(ItemStack itemStack) {
        Optional<ShinyItemProperty> shinyItemProperty =
                Models.Item.asWynnItemProperty(itemStack, ShinyItemProperty.class);

        return shinyItemProperty.isPresent()
                && shinyItemProperty.get().getShinyStat().isPresent();
    }
}
