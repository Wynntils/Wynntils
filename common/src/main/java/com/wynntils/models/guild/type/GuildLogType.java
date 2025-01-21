/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.guild.type;

import com.wynntils.utils.render.Texture;
import net.minecraft.ChatFormatting;

public enum GuildLogType {
    GENERAL(
            "General",
            Texture.GENERAL_LOG_ICON,
            ChatFormatting.AQUA,
            "screens.wynntils.customGuildLog.generalLogTooltip"),
    OBJECTIVES(
            "Objectives",
            Texture.OBJECTIVES_LOG_ICON,
            ChatFormatting.GREEN,
            "screens.wynntils.customGuildLog.objectivesLogTooltip"),
    WARS("Wars", Texture.WARS_LOG_ICON, ChatFormatting.RED, "screens.wynntils.customGuildLog.warsLogTooltip"),
    ECONOMY(
            "Economy",
            Texture.ECONOMY_LOG_ICON,
            ChatFormatting.YELLOW,
            "screens.wynntils.customGuildLog.economyLogTooltip"),
    PUBLIC_BANK(
            "Public Bank",
            Texture.PUBLIC_BANK_LOG_ICON,
            ChatFormatting.LIGHT_PURPLE,
            "screens.wynntils.customGuildLog.publicBankLogTooltip"),
    HIGH_RANKED_BANK(
            "HR Bank",
            Texture.HR_BANK_LOG_ICON,
            ChatFormatting.DARK_PURPLE,
            "screens.wynntils.customGuildLog.highRankedBankLogTooltip");

    private final String displayName;
    private final Texture icon;
    private final ChatFormatting color;
    private final String tooltipKey;

    GuildLogType(String displayName, Texture icon, ChatFormatting color, String tooltipKey) {
        this.displayName = displayName;
        this.icon = icon;
        this.color = color;
        this.tooltipKey = tooltipKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Texture getIcon() {
        return icon;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getTooltipKey() {
        return tooltipKey;
    }
}
