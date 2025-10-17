/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.redirects;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.REDIRECTS)
public class AbilityRefreshRedirectFeature extends Feature {
    private static final Pattern REFRESH_PATTERN = Pattern.compile("\\[⬤\\] (.+) has been refreshed!");

    @SubscribeEvent
    public void onChat(ChatMessageEvent.Match event) {
        if (event.getMessage().matches(REFRESH_PATTERN, StyleType.NONE)) {
            event.cancelChat();
            Managers.Notification.queueMessage(event.getMessage());
        }
    }
}
