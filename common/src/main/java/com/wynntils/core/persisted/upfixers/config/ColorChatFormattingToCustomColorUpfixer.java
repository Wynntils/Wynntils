/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import com.wynntils.utils.EnumUtils;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;

public class ColorChatFormattingToCustomColorUpfixer implements Upfixer {
    private static final Map<String, String> COLOR_MAP = Map.ofEntries(
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.BLACK), "#000000ff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.DARK_BLUE), "#0000aaff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.DARK_GREEN), "#00aa00ff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.DARK_AQUA), "#00aaaaff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.DARK_RED), "#aa0000ff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.DARK_PURPLE), "#aa00aaff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.GOLD), "#ffaa00ff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.GRAY), "#aaaaaaff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.DARK_GRAY), "#555555ff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.BLUE), "#5555ffff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.GREEN), "#55ff55ff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.AQUA), "#55ffffff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.RED), "#ff5555ff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.LIGHT_PURPLE), "#ff55ffff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.YELLOW), "#ffff55ff"),
            Map.entry(EnumUtils.toJsonFormat(ChatFormatting.WHITE), "#ffffffff"));

    private static final Set<String> CHAT_FORMATTING_CONFIG_KEYS = Set.of(
            "transcribeMessagesFeature.gavellianColor",
            "transcribeMessagesFeature.wynnicColor",
            "extendedSeasonLeaderboardFeature.guildHighlightColor",
            "chatMentionFeature.mentionColor",
            "shamanTotemTimerOverlay.firstTotemTextColor",
            "shamanTotemTimerOverlay.secondTotemTextColor",
            "shamanTotemTimerOverlay.thirdTotemTextColor",
            "shamanTotemTimerOverlay.fourthTotemTextColor");

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        for (String key : CHAT_FORMATTING_CONFIG_KEYS) {
            if (configObject.has(key)) {
                JsonPrimitive configValue = configObject.getAsJsonPrimitive(key);
                if (!configValue.isString()) continue;

                String customColorValue = COLOR_MAP.get(configValue.getAsString());
                if (customColorValue == null) continue;

                configObject.addProperty(key, customColorValue);
            }
        }

        return true;
    }
}
