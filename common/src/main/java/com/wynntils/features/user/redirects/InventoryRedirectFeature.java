/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.redirects;

import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.notifications.MessageContainer;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.ComponentUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.REDIRECTS)
public class InventoryRedirectFeature extends UserFeature {
    private static final Pattern INGREDIENT_POUCH_PICKUP_PATTERN = Pattern.compile("^§a\\+\\d+ §7.+§a to pouch$");
    private static final Pattern EMERALD_POUCH_PICKUP_PATTERN = Pattern.compile("§a\\+(\\d+)§7 Emeralds? §ato pouch");
    private static final Pattern POTION_STACK_PATTERN = Pattern.compile("§a\\+(\\d+)§7 potion §acharges?");

    private long lastEmeraldPouchPickup = 0;
    private MessageContainer emeraldPouchMessage = null;

    @Config
    public boolean redirectIngredientPouch = true;

    @Config
    public boolean redirectEmeraldPouch = true;

    @Config
    public boolean redirectPotionStack = true;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        lastEmeraldPouchPickup = 0;
        emeraldPouchMessage = null;
    }

    @SubscribeEvent
    public void onSubtitleSetText(SubtitleSetTextEvent event) {
        if (!redirectEmeraldPouch && !redirectIngredientPouch && !redirectPotionStack) {
            return;
        }

        Component component = event.getComponent();
        String codedString = ComponentUtils.getCoded(component);

        if (redirectIngredientPouch) {
            if (INGREDIENT_POUCH_PICKUP_PATTERN.matcher(codedString).matches()) {
                event.setCanceled(true);
                Managers.Notification.queueMessage(codedString);
                return;
            }
        }

        if (redirectEmeraldPouch) {
            Matcher matcher = EMERALD_POUCH_PICKUP_PATTERN.matcher(codedString);
            if (matcher.matches()) {
                event.setCanceled(true);

                // If the last emerald pickup event was less than 3 seconds ago, assume Wynn has relayed us an "updated"
                // emerald title
                // Edit the first message it gave us with the new amount
                // editMessage doesn't return the new MessageContainer, so we can just keep re-using the first one
                if (lastEmeraldPouchPickup > System.currentTimeMillis() - 3000 && emeraldPouchMessage != null) {
                    emeraldPouchMessage = Managers.Notification.editMessage(emeraldPouchMessage, codedString);
                } else {
                    emeraldPouchMessage = Managers.Notification.queueMessage(codedString);
                }

                lastEmeraldPouchPickup = System.currentTimeMillis();

                return;
            }
        }

        if (redirectPotionStack) {
            Matcher matcher = POTION_STACK_PATTERN.matcher(codedString);
            if (matcher.matches()) {
                event.setCanceled(true);
                String potionCount = matcher.group(1);
                String potionMessage = String.format("§a+%s Potion Charges", potionCount);
                Managers.Notification.queueMessage(potionMessage);

                return;
            }
        }
    }
}
