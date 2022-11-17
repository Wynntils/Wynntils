/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
public class ChatTimestampFeature extends UserFeature {
    @Config
    public String formatPattern = "HH:mm:ss";

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent event) {
        Component message = event.getMessage();

        // use the user specified formatter and if that formatter fails for whatever reason handle it as gracefully as we can
        MutableComponent timestamp;
        try {
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);

            timestamp = new TextComponent("§8[§7" + date.format(formatter) + "§8]§r ");
        } catch (Exception e) {
            timestamp = new TextComponent("§8[§7<Invalid time format string>§8]§r ");
        }

        timestamp.append(message);

        event.setMessage(timestamp);
    }
}
