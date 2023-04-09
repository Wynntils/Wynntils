/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.redirects;

import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.utils.mc.ComponentUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.REDIRECTS)
public class AbilityRefreshRedirectFeature extends Feature {
    private static final Pattern REFRESH_PATTERN = Pattern.compile("\\[⬤\\] (.+) has been refreshed!");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ChatMessageReceivedEvent event) {
        Matcher matcher = REFRESH_PATTERN.matcher(ComponentUtils.stripFormatting(event.getOriginalCodedString()));
        if (matcher.matches()) {
            event.setCanceled(true);

            Managers.Notification.queueMessage(event.getOriginalMessage());
        }
    }
}
