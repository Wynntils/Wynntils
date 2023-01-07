/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.tooltips;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.features.user.ItemFavoriteFeature;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.wynn.handleditems.items.game.GearBoxItem;
import com.wynntils.wynn.objects.EmeraldSymbols;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
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

        List<Component> tooltips = new ArrayList<>(event.getBaseTooltips());
        tooltips.addAll(getTooltipAddon(gearBoxItemOpt.get()));
        event.setTooltips(tooltips);
    }

    private List<Component> getTooltipAddon(GearBoxItem gearBoxItem) {
        List<Component> addon = new ArrayList<>();
        List<String> itemPossibilities = gearBoxItem.getItemPossibilities();
        ItemTier itemTier = gearBoxItem.getItemTier();

        if (itemPossibilities.isEmpty()) return addon; // nothing to put in tooltip

        addon.add(Component.translatable("feature.wynntils.itemGuess.possibilities"));

        Map<Integer, List<MutableComponent>> levelToItems = new TreeMap<>();

        for (String item : itemPossibilities) {
            ItemProfile profile = Managers.ItemProfiles.getItemsProfile(item);

            int level = (profile != null) ? profile.getLevelRequirement() : -1;

            MutableComponent itemDesc = Component.literal(item).withStyle(itemTier.getChatFormatting());

            if (ItemFavoriteFeature.INSTANCE.favoriteItems.contains(item)) {
                itemDesc.withStyle(ChatFormatting.UNDERLINE);
            }

            levelToItems.computeIfAbsent(level, i -> new ArrayList<>()).add(itemDesc);
        }

        for (Map.Entry<Integer, List<MutableComponent>> entry : levelToItems.entrySet()) {
            int level = entry.getKey();
            List<MutableComponent> itemsForLevel = entry.getValue();

            MutableComponent guesses = Component.literal("    ");

            guesses.append(Component.translatable("feature.wynntils.itemGuess.levelLine", level == -1 ? "?" : level));

            if (showGuessesPrice && level != -1) {
                guesses.append(Component.literal(" [")
                        .append(Component.literal(
                                        (itemTier.getItemIdentificationCost(level) + " " + EmeraldSymbols.E_STRING))
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
