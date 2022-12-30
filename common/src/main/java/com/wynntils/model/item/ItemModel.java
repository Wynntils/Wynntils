/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.handlers.item.ItemHandler;
import com.wynntils.model.item.annotators.AmplifierAnnotator;
import com.wynntils.model.item.annotators.CraftedConsumableAnnotator;
import com.wynntils.model.item.annotators.CraftedGearAnnotator;
import com.wynntils.model.item.annotators.DungeonKeyAnnotator;
import com.wynntils.model.item.annotators.EmeraldPouchAnnotator;
import com.wynntils.model.item.annotators.GatheringToolAnnotator;
import com.wynntils.model.item.annotators.GearAnnotator;
import com.wynntils.model.item.annotators.GearBoxAnnotator;
import com.wynntils.model.item.annotators.HealthPotionAnnotator;
import com.wynntils.model.item.annotators.HorseAnnotator;
import com.wynntils.model.item.annotators.IngredientAnnotator;
import com.wynntils.model.item.annotators.ManaPotionAnnotator;
import com.wynntils.model.item.annotators.MaterialAnnotator;
import com.wynntils.model.item.annotators.PowderAnnotator;
import com.wynntils.model.item.annotators.SkillPotionAnnotator;
import com.wynntils.model.item.annotators.TeleportScrollAnnotator;
import com.wynntils.model.item.annotators.XpPotionAnnotator;
import com.wynntils.model.item.annotators.gui.CosmeticTierAnnotator;
import com.wynntils.model.item.annotators.gui.DailyRewardMultiplierAnnotator;
import com.wynntils.model.item.annotators.gui.ServerAnnotator;
import com.wynntils.model.item.annotators.gui.SkillPointAnnotator;
import com.wynntils.model.item.annotators.gui.SoulPointAnnotator;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public class ItemModel extends Model {
    @Override
    public void init() {
        // For efficiency, register these annotator first
        Handlers.Item.registerAnnotator(new GearAnnotator());
        Handlers.Item.registerAnnotator(new GearBoxAnnotator());
        Handlers.Item.registerAnnotator(new IngredientAnnotator());
        Handlers.Item.registerAnnotator(new MaterialAnnotator());

        // Then alphabetically
        Handlers.Item.registerAnnotator(new AmplifierAnnotator());
        Handlers.Item.registerAnnotator(new CraftedConsumableAnnotator());
        Handlers.Item.registerAnnotator(new CraftedGearAnnotator());
        Handlers.Item.registerAnnotator(new DungeonKeyAnnotator());
        Handlers.Item.registerAnnotator(new EmeraldPouchAnnotator());
        Handlers.Item.registerAnnotator(new GatheringToolAnnotator());
        Handlers.Item.registerAnnotator(new HealthPotionAnnotator());
        Handlers.Item.registerAnnotator(new HorseAnnotator());
        Handlers.Item.registerAnnotator(new ManaPotionAnnotator());
        Handlers.Item.registerAnnotator(new PowderAnnotator());
        Handlers.Item.registerAnnotator(new SkillPotionAnnotator());
        Handlers.Item.registerAnnotator(new TeleportScrollAnnotator());
        Handlers.Item.registerAnnotator(new XpPotionAnnotator());

        // GUI handlers
        Handlers.Item.registerAnnotator(new CosmeticTierAnnotator());
        Handlers.Item.registerAnnotator(new DailyRewardMultiplierAnnotator());
        Handlers.Item.registerAnnotator(new ServerAnnotator());
        Handlers.Item.registerAnnotator(new SkillPointAnnotator());
        Handlers.Item.registerAnnotator(new SoulPointAnnotator());

        // This must be done last
        Handlers.Item.registerAnnotator(new FallbackAnnotator());
    }

    public static Optional<WynnItem> getWynnItem(ItemStack itemStack) {
        var annotationOpt = ItemHandler.getItemStackAnnotation(itemStack);
        if (annotationOpt.isEmpty()) return Optional.empty();
        if (!(annotationOpt.get() instanceof WynnItem wynnItem)) return Optional.empty();

        return Optional.of(wynnItem);
    }

    public static final class FallbackAnnotator implements ItemAnnotator {
        @Override
        public ItemAnnotation getAnnotation(ItemStack itemStack) {
            return new WynnItem();
        }
    }
}
