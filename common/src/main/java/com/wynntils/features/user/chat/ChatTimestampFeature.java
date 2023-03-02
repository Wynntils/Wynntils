/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.chat;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.utils.mc.McUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class ChatTimestampFeature extends UserFeature {
    @Config
    public String formatPattern = "HH:mm:ss";

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern, Locale.ROOT);

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        // Try to set the new format string and if it fails revert to the default
        try {
            formatter = DateTimeFormatter.ofPattern(formatPattern, Locale.ROOT);
        } catch (Exception e) {
            formatter = null;

            McUtils.sendMessageToClient(Component.translatable("feature.wynntils.chatTimestamp.invalidFormatMsg")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChat(ChatMessageReceivedEvent event) {
        if (formatter == null) return;

        Component message = event.getMessage();

        LocalDateTime date = LocalDateTime.now();
        MutableComponent timestamp = Component.literal("§8[§7" + date.format(formatter) + "§8]§r ");

        timestamp.append(message);

        event.setMessage(timestamp);
    }
}
