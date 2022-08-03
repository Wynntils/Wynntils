/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.ItemGuessProfile;
import com.wynntils.core.webapi.profiles.item.ItemProfile;
import com.wynntils.core.webapi.profiles.item.ItemTier;
import com.wynntils.core.webapi.profiles.item.ItemType;
import com.wynntils.features.user.tooltips.ItemGuessFeature;
import com.wynntils.utils.reference.EmeraldSymbols;
import com.wynntils.wc.utils.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class UnidentifiedItemStack extends WynnItemStack {

    private ItemGuessProfile guessProfile;
    private ItemTier tier;

    private final List<Component> tooltip;

    public UnidentifiedItemStack(ItemStack stack) {
        super(stack);

        tooltip = getOriginalTooltip();

        tier = ItemTier.fromComponent(getHoverName());
        if (tier == null) return;

        String itemType = itemName.split(" ", 2)[1];
        if (itemType == null) return;

        String levelRange = null;
        for (Component lineComp : tooltip) {
            String line = WynnUtils.normalizeBadString(lineComp.getString());
            if (line.contains("Lv. Range")) {
                levelRange = line.replace("- Lv. Range: ", "");
                break;
            }
        }

        if (levelRange == null) return;
        if (WebManager.getItemGuesses() == null || WebManager.getItemsMap() == null) return;

        guessProfile = WebManager.getItemGuesses().get(levelRange);
        if (guessProfile == null) return;

        Map<ItemTier, List<String>> rarityMap;
        try {
            rarityMap = guessProfile.getItems().get(ItemType.valueOf(itemType.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) { // itemType is invalid
            WynntilsMod.warn(String.format("ItemType was invalid for itemType: %s", itemType));
            return;
        }
        if (rarityMap == null) return;

        List<String> items = rarityMap.get(tier);
        if (items == null || items.isEmpty()) return;

        tooltip.add(new TranslatableComponent("feature.wynntils.itemGuess.possibilities"));

        Map<Integer, List<MutableComponent>> levelToItems = new TreeMap<>();

        for (String item : items) {
            ItemProfile profile = WebManager.getItemsMap().get(item);

            int level = (profile != null) ? profile.getLevelRequirement() : -1;

            MutableComponent itemDesc = new TextComponent(item).withStyle(tier.getChatFormatting());

            levelToItems.computeIfAbsent(level, i -> new ArrayList<>()).add(itemDesc);
        }

        for (Map.Entry<Integer, List<MutableComponent>> entry : levelToItems.entrySet()) {
            int level = entry.getKey();
            List<MutableComponent> itemsForLevel = entry.getValue();

            MutableComponent guesses = new TextComponent("  ");

            guesses.append(
                    new TranslatableComponent("feature.wynntils.itemGuess.levelline", level != -1 ? level : "?"));

            if (ItemGuessFeature.showGuessesPrice && level != -1) {
                guesses.append(new TextComponent(" [")
                        .append(new TextComponent(
                                        (tier.getItemIdentificationCost(level) + " " + EmeraldSymbols.E_STRING))
                                .withStyle(ChatFormatting.GREEN))
                        .append(new TextComponent("]"))
                        .withStyle(ChatFormatting.GRAY));
            }

            guesses.append("§7: ");

            guesses.append(itemsForLevel.stream()
                    .reduce((i, j) -> i.append(new TextComponent(", ").withStyle(ChatFormatting.GRAY))
                            .append(j))
                    .get());

            tooltip.add(guesses);
        }
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        return tooltip;
    }
}
