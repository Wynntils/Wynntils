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
        EnumMap<ItemTier, Integer> totalItems = new EnumMap<>(ItemTier.class);
        // Tracks count of sold or scrapped items
        int totalItemInteger = 0;
        // If we get emeralds, this will be the number of emeralds we get. If we get scrap, this will be the number of
        // scrap we get.
        String paymentString = "";
        Matcher messageMatcher = BLACKSMITH_MESSAGE_PATTERN.matcher(event.getCodedMessage());
        if (!messageMatcher.matches()) return;
        event.setCanceled(true);

        // Retrieve the color code of the item, and then match it to the item tier.
        Matcher itemMatcher = ITEM_PATTERN.matcher(messageMatcher.group(2)); // Second group contains all of the items.
        while (itemMatcher.find()) {
            ChatFormatting itemColor = ChatFormatting.getByCode(
                    itemMatcher.group(1).charAt(0)); // find the color code, then get the ChatFormatting of the tiem.
            ItemTier tierToIncrease = ItemTier.fromChatFormatting(itemColor);
            if (tierToIncrease == null) continue;
            totalItems.put(tierToIncrease, totalItems.getOrDefault(tierToIncrease, 0) + 1);
            totalItemInteger++;
        }

        // The final part of the message.
        {
            // Let's tally up the total number of items sold.
            StringBuilder messageCounts = new StringBuilder();
            for (ItemTier tier : ItemTier.values()) {
                messageCounts.append('/' + tier.getChatFormatting().toString() + totalItems.getOrDefault(tier, 0));
                messageCounts.append(ChatFormatting.LIGHT_PURPLE);
            }

            String pluralizer = totalItemInteger == 1 ? "" : "s";

            messageCounts.append(") item" + pluralizer + " for ");

            messageCounts.setCharAt(0, '(');

            // How many emeralds/scrap we got from the blacksmith.
            paymentString = messageMatcher.group(3);

            String messageCountsString = messageCounts.toString();

            // Now, we create the full message.
            StringBuilder sendableMessage = new StringBuilder();
            // Normal sale of items for emeralds.
            if (messageMatcher.group(1).equals("sold me")) {
                sendableMessage.append(ChatFormatting.LIGHT_PURPLE + "Sold " + totalItemInteger + " ");
                sendableMessage.append(messageCountsString);
                sendableMessage.append(ChatFormatting.GREEN + paymentString + EmeraldSymbols.EMERALDS
                        + ChatFormatting.LIGHT_PURPLE + ".");
            }
            // Scrapping items for scrap
            else {
                sendableMessage.append(ChatFormatting.LIGHT_PURPLE + "Scrapped " + totalItemInteger);
                sendableMessage.append(" item" + pluralizer + " for ");
                sendableMessage.append(
                        ChatFormatting.YELLOW + paymentString + " scrap" + ChatFormatting.LIGHT_PURPLE + ".");
            }

            String sendableMessageString = sendableMessage.toString();

            // Finally, we send the message.
            NotificationManager.queueMessage(sendableMessageString);
        }
    }
}
