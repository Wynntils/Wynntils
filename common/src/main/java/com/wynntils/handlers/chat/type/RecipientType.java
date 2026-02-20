/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.type;

import com.wynntils.core.text.StyledText;
import java.util.regex.Pattern;

public enum RecipientType {
    INFO(null, "Info"),
    CLIENTSIDE(null, "Clientside"),
    // Test in RecipientType_GLOBAL_pattern
    GLOBAL("^§7[\uE040-\uE059]{2}[\uE060-\uE069]{1,3}§r .+", "Global"),
    // Test in RecipientType_LOCAL_pattern
    LOCAL("^§f[\uE040-\uE059]{2}[\uE060-\uE069]{1,3}(?:§r)? .+", "Local"),
    // Test in RecipientType_GUILD_pattern
    GUILD("^§b((\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)).*$", "Guild"),
    // Test in RecipientType_PARTY_pattern
    PARTY("^§e((\uDAFF\uDFFC\uE005\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)) .*$", "Party"),
    // Test in RecipientType_PRIVATE_pattern
    PRIVATE(
            "^§#ddcc99ff((\uDAFF\uDFFC\uE007\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)) .* \uE003 .*:(§#ddcc99ff)? §f.*$",
            "Private"),
    // Test in RecipientType_SHOUT_pattern
    SHOUT(
            "^§#bd45ffff((\uDAFF\uDFFC\uE015\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)) (§o)?.+?(§r§#bd45ffff)? .+?§#bd45ffff shouts: .+$",
            "Shout"),
    // Test in RecipientType_PETS_pattern
    PETS(
            "^§6((\uDAFF\uDFFC\uE016\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)) (§o)?.+?(§r§6)?: §#ffdd99ff(§o)?.+$",
            "Pets"),
    GAME_MESSAGE("^§7[A-Z0-9].*$", "Game Message"); // Like dialogues but not uttered by an NPC

    private final Pattern pattern;
    private final String name;

    RecipientType(String pattern, String name) {
        this.pattern = (pattern == null ? null : Pattern.compile(pattern, Pattern.DOTALL));

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

    public String getName() {
        return name;
    }
}
