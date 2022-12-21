/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Managers;
import com.wynntils.features.user.ItemFavoriteFeature;
import com.wynntils.features.user.tooltips.ItemGuessFeature;
import com.wynntils.wynn.objects.EmeraldSymbols;
import com.wynntils.wynn.objects.profiles.ItemGuessProfile;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
import com.wynntils.wynn.objects.profiles.item.ItemType;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class UnidentifiedItemStack extends WynnItemStack {
    private final List<Component> tooltip;
    private ItemType itemType;
    private ItemTier itemTier;
    private List<String> itemPossibilities;

    public UnidentifiedItemStack(ItemStack stack) {
        super(stack);

        tooltip = getOriginalTooltip();

        if (!ItemGuessFeature.INSTANCE.isEnabled()) return;

        itemTier = ItemTier.fromComponent(getHoverName());
        if (itemTier == null) return;

        String itemTypeStr = itemName.split(" ", 2)[1];
        if (itemTypeStr == null) return;

        Optional<ItemType> oItemType = ItemType.fromString(itemTypeStr);
        if (oItemType.isEmpty()) {
            WynntilsMod.warn(String.format("ItemType was invalid for itemType: %s", itemTypeStr));
            return;
        }
        itemType = oItemType.get();

        String levelRange = null;
        for (Component lineComp : tooltip) {
            String line = WynnUtils.normalizeBadString(lineComp.getString());
            if (line.contains("Lv. Range")) {
                levelRange = line.replace("- Lv. Range: ", "");
                break;
            }
        }
        if (levelRange == null) return;

        ItemGuessProfile guessProfile = Managers.ItemProfiles.getItemGuess(levelRange);
        if (guessProfile == null) return;

        Map<ItemTier, List<String>> rarityMap = guessProfile.getItems().get(itemType);
        if (rarityMap == null) return;

        itemPossibilities = rarityMap.get(itemTier);
        if (itemPossibilities == null || itemPossibilities.isEmpty()) return;

        tooltip.add(Component.translatable("feature.wynntils.itemGuess.possibilities"));

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

            if (ItemGuessFeature.INSTANCE.showGuessesPrice && level != -1) {
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

                tooltip.add(guesses);
            }
        }
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        return tooltip;
    }

    public Optional<ItemType> getItemType() {
        return Optional.ofNullable(itemType);
    }

    public Optional<ItemTier> getItemTier() {
        return Optional.ofNullable(itemTier);
    }

    public List<String> getPossibleItems() {
        return itemPossibilities == null ? List.of() : itemPossibilities;
    }
}
