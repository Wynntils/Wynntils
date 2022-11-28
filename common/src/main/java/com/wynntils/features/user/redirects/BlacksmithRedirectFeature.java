/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.redirects;

import com.wynntils.core.chat.ChatModel;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.managers.Model;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.core.webapi.profiles.item.ItemTier;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import com.wynntils.wynn.objects.EmeraldSymbols;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.REDIRECTS)
public class BlacksmithRedirectFeature extends UserFeature {
    private static final Pattern BLACKSMITH_MESSAGE_PATTERN = Pattern.compile(
            "§5Blacksmith: §r§dYou (.+): (.+) for a total of §r§e(\\d+)§r§d (emeralds|scrap). It was a pleasure doing business with you.");
    private static final Pattern ITEM_PATTERN = Pattern.compile("§r§([fedacb53])(.+?)§r§d");

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(ChatModel.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChat(ChatMessageReceivedEvent event) {
        Matcher messageMatcher = BLACKSMITH_MESSAGE_PATTERN.matcher(event.getOriginalCodedMessage());
        if (!messageMatcher.matches()) return;
        event.setCanceled(true);

        EnumMap<ItemTier, Integer> totalItems = new EnumMap<>(ItemTier.class);
        // How many emeralds/scrap we got from the transaction.
        String paymentString = messageMatcher.group(3);
        // Tracks count of sold or scrapped items
        int totalItemInteger = 0;
        // Full message to send to the user.
        String sendableMessage = "";
        // Should we use item, or items?
        String itemPluralizer = "";

        // This is for selling items for emeralds.
        if (messageMatcher.group(1).equals("sold me")) {

            // Retrieve the color code of the item, and then match it to the item tier.
            Matcher itemMatcher =
                    ITEM_PATTERN.matcher(messageMatcher.group(2)); // Second group contains all of the items.
            // Tally up the items that we sold.
            while (itemMatcher.find()) {
                totalItemInteger++;
                char itemColorCode = itemMatcher.group(1).charAt(0);
                if (itemColorCode == 'd') continue; // This is for non-tiered crafting items.
                ChatFormatting itemColor = ChatFormatting.getByCode(
                        itemColorCode); // find the color code to find ChatFormatting and ascertain the tier.
                ItemTier tierToIncrease = ItemTier.fromChatFormatting(itemColor);
                if (tierToIncrease == null) continue;
                totalItems.put(tierToIncrease, totalItems.getOrDefault(tierToIncrease, 0) + 1);
            }
            itemPluralizer = totalItemInteger == 1 ? "item" : "items";

            // Build up the string that outlines how many items were sold in what tier (0/0/0/0/0/0/0/0).
            StringBuilder countByTier = new StringBuilder();
            for (ItemTier tier : ItemTier.values()) {
                countByTier.append('/');
                countByTier.append(tier.getChatFormatting().toString());
                countByTier.append(totalItems.getOrDefault(tier, 0));
                countByTier.append(ChatFormatting.LIGHT_PURPLE);
            }
            countByTier.append(")");
            countByTier.setCharAt(0, '(');

            String countByTierString = countByTier.toString();

            // Sold 1 (1/0/0/0/0/0/0/0) item for 4e.
            sendableMessage = String.format(
                    "§r§dSold %d %s %s for §r§a%s%s§r§d.",
                    totalItemInteger, itemPluralizer, countByTierString, paymentString, EmeraldSymbols.EMERALDS);
        }
        // Scrapping items for scrap.
        else {
            Matcher itemMatcher =
                    ITEM_PATTERN.matcher(messageMatcher.group(2)); // Second group contains all of the items.
            while (itemMatcher.find()) {
                totalItemInteger++;
            }
            itemPluralizer = totalItemInteger == 1 ? "item" : "items";

            sendableMessage = String.format(
                    "§r§dScrapped %d %s for §r§a%s scrap%s§r§d.", totalItemInteger, itemPluralizer, paymentString);
        }

        // Finally, we send the message.
        NotificationManager.queueMessage(sendableMessage);
    }
}
