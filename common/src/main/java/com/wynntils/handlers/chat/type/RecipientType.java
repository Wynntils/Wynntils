/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.type;

import com.wynntils.core.text.StyledText;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

public enum RecipientType {
    INFO(null, "Info"),
    CLIENTSIDE(null, "Clientside"),
    WYNNTILS("2", '\uE100', null, "Wynntils"),
    // Test in RecipientType_GLOBAL_pattern
    GLOBAL("^§7[\uE040-\uE059]{2}[\uE060-\uE069]{1,3}§r .+", "Global"),
    // Test in RecipientType_LOCAL_pattern
    LOCAL("^§f[\uE040-\uE059]{2}[\uE060-\uE069]{1,3}(?:§r)? .+", "Local"),
    // Test in RecipientType_GUILD_pattern
    GUILD("b", '\uE006', null, "Guild"),
    // Test in RecipientType_PARTY_pattern
    PARTY("e", '\uE005', null, "Party"),
    // Test in RecipientType_PRIVATE_pattern
    PRIVATE("#ddcc99ff", '\uE007', ".* \uE003 .*:(§#ddcc99ff)? §f.+", "Private"),
    // Test in RecipientType_SHOUT_pattern
    SHOUT("#bd45ffff", '\uE015', "(§o)?.+?(§r§#bd45ffff)? .+?§#bd45ffff shouts: .+", "Shout"),
    // Test in RecipientType_PETS_pattern
    PETS("6", '\uE016', "(§o)?.+?(§r§6)?: §#ffdd99ff(§o)?.+", "Pets"),
    // Test in RecipientType_BOMB_BELL_pattern
    BOMB_BELL("#fddd5cff", '\uE01E', null, "Bomb Bell"),
    // Test in RecipientType_BOMB_pattern
    BOMB("#a0c84bff", '\uE014', null, "Bomb"),
    // Test in RecipientType_WORLD_EVENT_pattern
    WORLD_EVENT("#00bdbfff", '\uE00D', null, "World Event"),
    // Test in RecipientType_SYSTEM_INFO_pattern
    SYSTEM_INFO("#a0aec0ff", '\uE01B', null, "System Info"),
    // Test in RecipientType_MERCHANT_pattern
    MERCHANT("5", '\uE00A', null, "Merchant"), // Blacksmith, Trade Market, Merchants, Party Finder
    // Test in RecipientType_ERROR_OR_WARNING_pattern
    ERROR_OR_WARNING("4", '\uE008', null, "Error or Warning"),
    RAID("#d6401eff", '\uE009', null, "Raid"),
    // Test in RecipientType_COMMAND_pattern
    COMMAND("a", '\uE008', null, "Command"),
    GAME_MESSAGE("^§7[A-Z0-9].*$", "Game Message"); // Like dialogues but not uttered by an NPC

    private final Pattern pattern;
    private final TextColor prefixColor;
    private final char prefixIcon;
    private final String name;

    RecipientType(String pattern, String name) {
        this.pattern = (pattern == null ? null : Pattern.compile(pattern, Pattern.DOTALL));
        this.prefixColor = null;
        this.prefixIcon = '\0';
        this.name = name;
    }

    RecipientType(String color, char icon, String contentPattern, String name) {
        StringBuilder builder = new StringBuilder()
                .append("^§")
                .append(color)
                .append("((\uDAFF\uDFFC")
                .append(icon)
                .append("\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)) (?<content>")
                .append(contentPattern == null ? ".*" : contentPattern)
                .append(")$");
        this.pattern = Pattern.compile(builder.toString(), Pattern.DOTALL);
        if (color.charAt(0) == '#') {
            // The complete color string includes alpha, but we are only interested in the rgb part
            this.prefixColor = TextColor.fromRgb(Integer.parseInt(color.substring(1, 7), 16));
        } else {
            this.prefixColor = TextColor.fromLegacyFormat(ChatFormatting.getByCode(color.charAt(0)));
        }
        this.prefixIcon = icon;
        this.name = name;
    }

    public boolean matchPattern(StyledText msg) {
        if (pattern == null) return false;

        return msg.getMatcher(pattern).matches();
    }

    public static RecipientType fromName(String string) {
        for (RecipientType type : values()) {
            if (type.name.equalsIgnoreCase(string)) {
                return type;
            }
        }

        return null;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getName() {
        return name;
    }

    public TextColor getPrefixColor() {
        return prefixColor;
    }

    public char getPrefixIcon() {
        return prefixIcon;
    }
}
