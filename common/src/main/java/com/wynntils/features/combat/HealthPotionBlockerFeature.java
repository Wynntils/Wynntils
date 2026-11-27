/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.elements.type.PotionType;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.items.items.game.MultiHealthPotionItem;
import com.wynntils.models.items.items.game.PotionItem;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class HealthPotionBlockerFeature extends Feature {
    @Persisted
    private final Config<Integer> threshold = new Config<>(95);

    public HealthPotionBlockerFeature() {
        super(ProfileDefault.onlyDefault());
    }

    @SubscribeEvent
    public void onPotionUse(UseItemEvent event) {
        if (checkPotionUse()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPotionUseOn(PlayerInteractEvent.Interact event) {
        if (checkPotionUse()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPotionUseOn(PlayerInteractEvent.InteractAt event) {
        if (checkPotionUse()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPotionUseOn(PlayerInteractEvent.RightClickBlock event) {
        if (checkPotionUse()) {
            event.setCanceled(true);
        }
    }

    private boolean checkPotionUse() {
        ItemStack itemStack = McUtils.inventory().getSelectedItem();
        if (!isHealingPotion(itemStack)) return false;
        if (Models.WorldState.inCharacterWardrobe()) return false;

        Optional<CappedValue> healthOpt = Models.CharacterStats.getHealth();
        if (healthOpt.isEmpty()) {
            WynntilsMod.warn("No health information available for health potion blocker check.");
            return false;
        }

        CappedValue health = healthOpt.get();
        StatusEffect overHealth = Models.StatusEffect.searchStatusEffectByName("Extra Health");

        int percentage = overHealth != null
                ? (int) Math.round(((health.current() - overHealth.getModifierValue()) / health.max()) * 100)
                : health.getPercentageInt();

        int limit = threshold.get();
        if (percentage < limit) return false;

        MutableComponent response;
        if (overHealth != null) {
            response = Component.translatable("feature.wynntils.healthPotionBlocker.aboveThreshold", limit);
        } else if (percentage < 100) {
            response = Component.translatable("feature.wynntils.healthPotionBlocker.thresholdReached", percentage);
        } else {
            response = Component.translatable("feature.wynntils.healthPotionBlocker.healthFull");
        }

        Managers.Notification.queueMessage(response.withStyle(ChatFormatting.RED));
        return true;
    }

    private boolean isHealingPotion(ItemStack itemStack) {
        Optional<MultiHealthPotionItem> healthPotionOpt =
                Models.Item.asWynnItem(itemStack, MultiHealthPotionItem.class);
        Optional<PotionItem> potionOpt = Models.Item.asWynnItem(itemStack, PotionItem.class);
        Optional<CraftedConsumableItem> craftedConsumableOpt =
                Models.Item.asWynnItem(itemStack, CraftedConsumableItem.class);

        if (healthPotionOpt.isEmpty() && potionOpt.isEmpty() && craftedConsumableOpt.isEmpty()) return false;

        // Check if potion is a healing potion
        if (potionOpt.isPresent()) {
            return (potionOpt.get().getType() == PotionType.HEALING);
        }

        // Check if crafted potion is a health potion
        if (craftedConsumableOpt.isPresent()) {
            return craftedConsumableOpt.get().isHealing();
        }

        // Multi health potions are always healing potions
        return true;
    }
}
