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
        Matcher messageMatcher = BLACKSMITH_MESSAGE_PATTERN.matcher(event.getCodedMessage());
        if (!messageMatcher.matches()) return;
        event.setCanceled(true);

        EnumMap<ItemTier, Integer> totalItems = new EnumMap<>(ItemTier.class);
        // How many emeralds/scrap we got from the transaction.
        String paymentString = messageMatcher.group(3);
        // Tracks count of sold or scrapped items
        int totalItemInteger = 0;
        // Full message to send to the user.
        String sendableMessage = "";

        // This is for selling items for emeralds.
        if (messageMatcher.group(1).equals("sold me")) {

            // Retrieve the color code of the item, and then match it to the item tier.
            Matcher itemMatcher = ITEM_PATTERN.matcher(messageMatcher.group(2)); // Second group contains all of the items.
            // Tally up the items that we sold.
            while (itemMatcher.find()) {
                ChatFormatting itemColor = ChatFormatting.getByCode(itemMatcher
                        .group(1)
                        .charAt(0)); // find the color code, then get the ChatFormatting of the item to match it to a tier.
                ItemTier tierToIncrease = ItemTier.fromChatFormatting(itemColor);
                if (tierToIncrease == null) continue;
                totalItems.put(tierToIncrease, totalItems.getOrDefault(tierToIncrease, 0) + 1);
                totalItemInteger++;
            }
            String itemPluralizer = totalItemInteger == 1 ? "item" : "items";

                // Build up the string that outlines how many items were sold in what tier (0/0/0/0/0/0/0/0).
                StringBuilder countByTier = new StringBuilder();
                for (ItemTier tier : ItemTier.values()) {
                    countByTier.append('/' + tier.getChatFormatting().toString() + totalItems.getOrDefault(tier, 0));
                    countByTier.append(ChatFormatting.LIGHT_PURPLE);
                }
                countByTier.append(")");
                countByTier.setCharAt(0, '(');

                String countByTierString = countByTier.toString();
                
                // Sold 1 (1/0/0/0/0/0/0/0) item for 4e.
                sendableMessage = String.format("%sSold %d %s %s for %s%s%s%s.", ChatFormatting.LIGHT_PURPLE, totalItemInteger, itemPluralizer, countByTierString, ChatFormatting.GREEN, paymentString, EmeraldSymbols.EMERALDS, ChatFormatting.LIGHT_PURPLE);
            }
        // Scrapping items for scrap.
        else
        {
            Matcher itemMatcher = ITEM_PATTERN.matcher(messageMatcher.group(2)); // Second group contains all of the items.
            while (itemMatcher.find()) {
                totalItemInteger++;
            }
            String itemPluralizer = totalItemInteger == 1 ? "" : "s";

            sendableMessage = String.format("%sScrapped %d %s for %s%s scrap%s.", ChatFormatting.LIGHT_PURPLE, totalItemInteger, itemPluralizer, ChatFormatting.YELLOW, paymentString, ChatFormatting.LIGHT_PURPLE);
        }

        // Finally, we send the message.
        NotificationManager.queueMessage(sendableMessage);
    }
}
