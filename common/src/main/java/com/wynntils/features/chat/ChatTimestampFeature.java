/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.utils.mc.McUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.CHAT)
public class ChatTimestampFeature extends Feature {
    @RegisterConfig
    public final Config<String> formatPattern = new Config<>("HH:mm:ss");

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern.get(), Locale.ROOT);

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        // Try to set the new format string and if it fails revert to the default
        try {
            formatter = DateTimeFormatter.ofPattern(formatPattern.get(), Locale.ROOT);
        } catch (Exception e) {
            formatter = null;

            McUtils.sendErrorToClient(I18n.get("feature.wynntils.chatTimestamp.invalidFormatMsg"));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChat(ChatMessageReceivedEvent event) {
        if (formatter == null) return;

        Component message = event.getMessage();

        LocalDateTime date = LocalDateTime.now();
        MutableComponent timestamp = Component.empty()
                .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(date.format(formatter)).withStyle(ChatFormatting.GRAY))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY));

        timestamp.append(message);

        event.setMessage(timestamp);
    }
}
