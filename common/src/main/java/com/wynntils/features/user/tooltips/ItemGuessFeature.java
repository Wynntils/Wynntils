/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.tooltips;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.TOOLTIPS)
public class ItemGuessFeature extends UserFeature {
    @Config
    public boolean showGuessesPrice = true;

    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        Optional<GearBoxItem> gearBoxItemOpt = Models.Item.asWynnItem(event.getItemStack(), GearBoxItem.class);
        if (gearBoxItemOpt.isEmpty()) return;

        List<Component> tooltips = LoreUtils.appendTooltip(
                event.getItemStack(), event.getTooltips(), getTooltipAddon(gearBoxItemOpt.get()));
        event.setTooltips(tooltips);
    }

    private List<Component> getTooltipAddon(GearBoxItem gearBoxItem) {
        List<Component> addon = new ArrayList<>();
        List<String> itemPossibilities = gearBoxItem.getItemPossibilities();
        GearTier gearTier = gearBoxItem.getGearTier();

        if (itemPossibilities.isEmpty()) return addon; // nothing to put in tooltip

        addon.add(Component.literal("- ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.translatable("feature.wynntils.itemGuess.possibilities")
                        .withStyle(ChatFormatting.GRAY)));

        Map<Integer, List<MutableComponent>> levelToItems = new TreeMap<>();

        for (String itemName : itemPossibilities) {
            GearInfo gearInfo = Models.Gear.getGearInfo(itemName);

            int level = (gearInfo != null) ? gearInfo.requirements().level() : -1;

            MutableComponent itemDesc = Component.literal(itemName).withStyle(gearTier.getChatFormatting());

            if (Models.Favorites.isFavorite(itemName)) {
                itemDesc.withStyle(ChatFormatting.UNDERLINE);
            }

            levelToItems.computeIfAbsent(level, i -> new ArrayList<>()).add(itemDesc);
        }

        for (Map.Entry<Integer, List<MutableComponent>> entry : levelToItems.entrySet()) {
            int level = entry.getKey();
            List<MutableComponent> itemsForLevel = entry.getValue();

            MutableComponent guesses = Component.literal("    ");

            guesses.append(Component.literal("- ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.translatable("feature.wynntils.itemGuess.levelLine", level == -1 ? "?" : level)
                            .withStyle(ChatFormatting.GRAY)));

            if (showGuessesPrice && level != -1) {
                guesses.append(Component.literal(" [")
                        .append(Component.literal((gearTier.getGearIdentificationCost(level) + " "
                                        + EmeraldUnits.EMERALD.getSymbol()))
                                .withStyle(ChatFormatting.GREEN))
                        .append(Component.literal("]"))
                        .withStyle(ChatFormatting.GRAY));
            }

            guesses.append("§7: ");

            Optional<MutableComponent> itemsComponent = itemsForLevel.stream()
                    .reduce((i, j) -> i.append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                            .append(j));

            if (itemsComponent.isPresent()) {
                guesses.append(itemsComponent.get());

                addon.add(guesses);
            }
        }

        return addon;
    }
}
