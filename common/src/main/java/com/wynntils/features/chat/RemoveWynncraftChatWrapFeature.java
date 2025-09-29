/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.utils.mc.StyledTextUtils;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.CHAT)
public class RemoveWynncraftChatWrapFeature extends Feature {
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChatReceived(ChatMessageEvent.MatchingEvent.EditableEvent event) {
        event.setMessage(StyledTextUtils.unwrap(event.getMessage()));
    }
}
