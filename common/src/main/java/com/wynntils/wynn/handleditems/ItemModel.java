/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.handlers.item.ItemHandler;
import com.wynntils.wynn.handleditems.annotators.game.AmplifierAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.CraftedConsumableAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.CraftedGearAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.DungeonKeyAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.EmeraldPouchAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.GatheringToolAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.GearAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.GearBoxAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.HealthPotionAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.HorseAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.IngredientAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.ManaPotionAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.MaterialAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.PowderAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.SkillPotionAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.TeleportScrollAnnotator;
import com.wynntils.wynn.handleditems.annotators.game.XpPotionAnnotator;
import com.wynntils.wynn.handleditems.annotators.gui.CosmeticTierAnnotator;
import com.wynntils.wynn.handleditems.annotators.gui.DailyRewardMultiplierAnnotator;
import com.wynntils.wynn.handleditems.annotators.gui.ServerAnnotator;
import com.wynntils.wynn.handleditems.annotators.gui.SkillPointAnnotator;
import com.wynntils.wynn.handleditems.annotators.gui.SoulPointAnnotator;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public class ItemModel extends Model {
    @Override
    public void init() {
        // For efficiency, register these annotators first
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

    public Optional<WynnItem> getWynnItem(ItemStack itemStack) {
        Optional<ItemAnnotation> annotationOpt = ItemHandler.getItemStackAnnotation(itemStack);
        if (annotationOpt.isEmpty()) return Optional.empty();
        if (!(annotationOpt.get() instanceof WynnItem wynnItem)) return Optional.empty();

        return Optional.of(wynnItem);
    }

    public <T extends WynnItem> Optional<T> asWynnItem(ItemStack itemStack, Class<T> clazz) {
        Optional<ItemAnnotation> annotationOpt = ItemHandler.getItemStackAnnotation(itemStack);
        if (annotationOpt.isEmpty()) return Optional.empty();
        if (!(annotationOpt.get() instanceof WynnItem wynnItem)) return Optional.empty();
        if (wynnItem.getClass() != clazz) return Optional.empty();

        return Optional.of((T) wynnItem);
    }

    public static final class FallbackAnnotator implements ItemAnnotator {
        @Override
        public ItemAnnotation getAnnotation(ItemStack itemStack) {
            return new WynnItem();
        }
    }
}
