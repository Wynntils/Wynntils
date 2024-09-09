/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.AddGuiMessageLineEvent;
import com.wynntils.mc.event.RenderChatTimestampEvent;
import com.wynntils.mc.extension.GuiMessageLineExtension;
import com.wynntils.utils.mc.McUtils;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.CHAT)
public class ChatTimestampFeature extends Feature {
    @Persisted
    public final Config<String> formatPattern = new Config<>("HH:mm:ss");

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern.get(), Locale.ROOT);

    @Override
    protected void onConfigUpdate(Config<?> config) {
        // Try to set the new format string and if it fails revert to the default
        try {
            formatter = DateTimeFormatter.ofPattern(formatPattern.get(), Locale.ROOT);
        } catch (Exception e) {
            formatter = null;

            McUtils.sendErrorToClient(I18n.get("feature.wynntils.chatTimestamp.invalidFormatMsg"));
        }
    }

    // We only format the timestamp when adding the line to improve performance
    @SubscribeEvent
    public void onGuiMessageLineAdd(AddGuiMessageLineEvent event) {
        if (event.getIndex() != 0 || formatter == null) return;

        GuiMessageLineExtension extension = (GuiMessageLineExtension) (Object) event.getLine();

        extension.setTimestamp(Component.empty()
                .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(extension.getCreated().format(formatter))
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY)));
    }

    @SubscribeEvent
    public void onTimestampRender(RenderChatTimestampEvent event) {
        event.setRendered(true);
    }
}
