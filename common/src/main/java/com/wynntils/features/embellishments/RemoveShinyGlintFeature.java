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
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.utils.mc.McUtils;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
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

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Pre event) {
        removeOrReplaceGlint(event.getItemStack());
    }

    @Override
    public void onEnable() {
        if (McUtils.player() == null) return;

        McUtils.inventory().items.forEach(this::removeOrReplaceGlint);
    }

    private void removeOrReplaceGlint(ItemStack itemStack) {
        Optional<ShinyItemProperty> shinyItemProperty =
                Models.Item.asWynnItemProperty(itemStack, ShinyItemProperty.class);

        if (shinyItemProperty.isEmpty()) return;
        if (shinyItemProperty.isPresent()
                && shinyItemProperty.get().getShinyStat().isEmpty()) return;

        // Weapons use the potion contents DataComponent, armor uses the dye color
        PotionContents itemStackPotionContents = itemStack.get(DataComponents.POTION_CONTENTS);
        if (itemStackPotionContents == null) {
            DyedItemColor dyeColor = itemStack.get(DataComponents.DYED_COLOR);

            if (dyeColor == null) return;
            if (dyeColor.rgb() != SHINY_GLINT_COLOR) return;

            itemStack.remove(DataComponents.DYED_COLOR);

            // Give it the enchanted effect similar to how shinies were displayed prior to the introduction of glints
            if (replaceGlint.get()) {
                itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        } else {
            Optional<Integer> potionColor = itemStackPotionContents.customColor();

            if (potionColor.isEmpty()) return;
            if (potionColor.get() != SHINY_GLINT_COLOR) return;

            PotionContents removedContents = new PotionContents(
                    itemStackPotionContents.potion(),
                    Optional.empty(),
                    itemStackPotionContents.customEffects(),
                    itemStackPotionContents.customName());
            itemStack.set(DataComponents.POTION_CONTENTS, removedContents);

            // Give it the enchanted effect similar to how shinies were displayed prior to the introduction of glints
            if (replaceGlint.get()) {
                itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }
    }
}
