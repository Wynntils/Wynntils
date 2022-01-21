/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.*;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.ItemGuessProfile;
import com.wynntils.mc.event.InventoryRenderEvent;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wc.objects.ItemTier;
import com.wynntils.wc.objects.ItemType;
import com.wynntils.wc.utils.ItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemGuessFeature extends Feature {
    {
        apis.add(WebManager::getItemGuessesProvider);
    }

    @SubscribeEvent
    public static void onInventoryRender(InventoryRenderEvent e) {
        if (!WynnUtils.onWorld()) return;

        Slot hoveredSlot = e.getHoveredSlot();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack stack = hoveredSlot.getItem();

        if (ItemUtils.hasMarker(stack, "itemGuesses")) return;
        if (!ItemMatchers.isUnidentified(stack)) return;

        ItemUtils.addMarker(stack, "itemGuesses");

        String name =
                WynnUtils.normalizeBadString(
                        ChatFormatting.stripFormatting(stack.getDisplayName().getString()));
        String itemType = name.split(" ", 3)[1];
        String levelRange = null;

        List<String> lore = ItemUtils.getLore(stack);

        for (String line : lore) {
            System.out.println("Line: " + line);
            if (line.contains("Lv. Range")) {
                levelRange = ChatFormatting.stripFormatting(line).replace("- Lv. Range: ", "");
                break;
            }
        }

        if (itemType == null || levelRange == null) return;

        ItemGuessProfile igp = WebManager.getItemGuessesProvider().getValue().get(levelRange);
        if (igp == null) return;

        Map<ItemTier, List<String>> rarityMap = igp.getItems().get(ItemType.valueOf(itemType));
        if (rarityMap == null) return;

        ItemTier tier = ItemTier.fromComponent(stack.getDisplayName());
        if (tier == null) return;

        List<String> items = rarityMap.get(tier);
        if (items == null) return;

        StringBuilder itemNamesAndCosts = new StringBuilder();
        for (String possibleItem : items) {
            // ItemProfile itemProfile = WebManager.getItems().get(possibleItem);

            String itemDescription = tier.getChatFormatting() + possibleItem;

            //        (UtilitiesConfig.INSTANCE.favoriteItems.contains(possibleItem) ? UNDERLINE :
            // "") + possibleItem; // underline favs
            /*
            if (UtilitiesConfig.Identifications.INSTANCE.showGuessesPrice && itemProfile != null) {
                int level = itemProfile.getRequirements().getLevel();
                int itemCost = tier.getItemIdentificationCost(level);
                itemDescription += GRAY + " [" + GREEN + itemCost + " " + EmeraldSymbols.E_STRING + GRAY + "]";
            }

             */
            if (itemNamesAndCosts.length() > 0) {
                itemNamesAndCosts.append(ChatFormatting.GRAY).append(", ");
            }

            itemNamesAndCosts.append(itemDescription);
        }

        ItemUtils.getLoreTag(stack)
                .add(
                        StringTag.valueOf(
                                ItemUtils.toLoreForm(
                                        ChatFormatting.GREEN
                                                + "- "
                                                + ChatFormatting.GRAY
                                                + "Possibilities: "
                                                + itemNamesAndCosts)));
    }

    @Override
    public PerformanceImpact getPerformanceImpact() {
        return PerformanceImpact.SMALL;
    }

    @Override
    public GameplayImpact getGameplayImpact() {
        return GameplayImpact.LARGE;
    }

    @Override
    public Stability getStability() {
        return Stability.STABLE;
    }
}
