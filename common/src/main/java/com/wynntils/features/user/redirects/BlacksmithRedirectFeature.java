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
import com.wynntils.mc.utils.ComponentUtils;
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
    private static final Pattern BLACKSMITH_PATTERN = Pattern.compile("Blacksmith: (.+). It was a pleasure doing business with you.");

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(ChatModel.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChat(ChatMessageReceivedEvent event) {
        Matcher matcher = BLACKSMITH_PATTERN.matcher(ComponentUtils.stripFormatting(event.getOriginalCodedMessage()));
        int totalItemInteger = 0;
        if (matcher.matches()) {
            event.setCanceled(true);

            // Tracks count of sold or scrapped items
            EnumMap<ItemTier, Integer> totalItems = new EnumMap(ItemTier.class);

            String parseable_message = event.getOriginalCodedMessage();

            for (String fragment : parseable_message.split("§")) {
                // Fragments without any item data should be ignored.
                if (fragment.equals("dYou sold me: ")
                        || fragment.equals("dYou scrapped: ")
                        || fragment.equals("d, ")
                        || fragment.equals("d and ")
                        || fragment.equals("d for a total of ")
                        || fragment.equals("5Blacksmith: ")) {
                    continue;
                }
                // The final part of the message.
                if (fragment.matches("e\\d+")) {
                    for (ItemTier tier : ItemTier.values()) {
                        totalItemInteger += totalItems.getOrDefault(tier, 0);
                    }

                    // Let's tally up the total number of items sold.
                    StringBuilder messageCounts = new StringBuilder();
                    for (ItemTier tier : ItemTier.values()) {
                        messageCounts.append(
                                '/' + tier.getChatFormatting().toString() + totalItems.getOrDefault(tier, 0));
                        messageCounts.append(ChatFormatting.LIGHT_PURPLE);
                    }

                    messageCounts.append(") item(s) for ");
                    messageCounts.setCharAt(0, '(');

                    String messageCountsString = messageCounts.toString();

                    // Now, we create the full message.
                    StringBuilder sendableMessage = new StringBuilder();
                    // Normal sale of items for emeralds.
                    if (parseable_message.split(" ")[2].equals("sold")) {
                        sendableMessage.append(ChatFormatting.LIGHT_PURPLE + "Sold " + totalItemInteger + " ");
                        sendableMessage.append(messageCountsString);
                        sendableMessage.append(ChatFormatting.GREEN + fragment.replace("e", "")
                                + EmeraldSymbols.EMERALDS + ChatFormatting.LIGHT_PURPLE + ".");
                    }
                    // Scrapping items for scrap
                    else {
                        sendableMessage.append(ChatFormatting.LIGHT_PURPLE + "Scrapped " + totalItemInteger + " ");
                        sendableMessage.append(ChatFormatting.YELLOW + fragment.replace("e", "") + " scrap"
                                + ChatFormatting.LIGHT_PURPLE + ".");
                    }

                    String sendableMessageString = sendableMessage.toString();

                    // Finally, we send the message.
                    NotificationManager.queueMessage(sendableMessageString);
                }
            }
        }
    }
}
