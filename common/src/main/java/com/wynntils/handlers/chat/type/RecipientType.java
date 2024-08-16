/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.type;

import com.wynntils.core.text.StyledText;
import java.util.regex.Pattern;

public enum RecipientType {
    INFO(null, null, "Info"),
    CLIENTSIDE(null, null, "Clientside"),
    // Test in RecipientType_NPC_foregroundPattern
    NPC("^§7\\[\\d+\\/\\d+\\](?:§.)? ?§[25] ?.+: ?§..*$", "^§8\\[\\d+\\/\\d+\\] .+: ?§..*$", "NPC"),
    // Test in RecipientType_GLOBAL_foregroundPattern and RecipientType_GLOBAL_backgroundPattern
    GLOBAL("^§7\uE056\uE042[\uE060-\uE069]{1,3}§r .+", "§7\uE056\uE042[\uE060-\uE069]{1,3}§r .+", "Global"),
    // Test in RecipientType_LOCAL_foregroundPattern and RecipientType_LOCAL_backgroundPattern
    LOCAL("^§f\uE056\uE042[\uE060-\uE069]{1,3}(?:§r)? .+", "^§8\uE056\uE042[\uE060-\uE069]{1,3}§r .+", "Local"),
    // Test in RecipientType_GUILD_foregroundPattern and RecipientType_GUILD_backgroundPattern
    GUILD(
            "^§b((\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)).*$",
            "^§8((\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)).*$",
            "Guild"),
    // Test in RecipientType_PARTY_foregroundPattern and RecipientType_PARTY_backgroundPattern
    PARTY(
            "^§e((\uDAFF\uDFFC\uE005\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)) .*$",
            "^§8((\uDAFF\uDFFC\uE005\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)) .*$",
            "Party"),
    // Test in RecipientType_PRIVATE_foregroundPattern and RecipientType_PRIVATE_backgroundPattern
    PRIVATE(
            "^§6((\uDAFF\uDFFC\uE007\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(\uDAFF\uDFFC\uE001\uDB00\uDC06)) .* \uE003 .*:§6 §f.*$",
            "^§8\uDAFF\uDFFC.* .* \uE003 .*:§8 .*$",
            "Private"),
    SHOUT("^§5.* \\[[A-Z0-9]+\\] shouts: §d.*$", "^(§8)?.* \\[[A-Z0-9]+\\] shouts: §7.*$", "Shout"),
    PETS("^§2(.*): §a(.*)$", "^§8(.*): §7(.*)$", "Pets"),
    GAME_MESSAGE("^§7[A-Z0-9].*$", null, "Game Message"); // Like dialogues but not uttered by an NPC

    private final Pattern foregroundPattern;
    private final Pattern backgroundPattern;
    private final String name;

    RecipientType(String foregroundPattern, String backgroundPattern, String name) {
        this.foregroundPattern =
                (foregroundPattern == null ? null : Pattern.compile(foregroundPattern, Pattern.DOTALL));
        this.backgroundPattern =
                (backgroundPattern == null ? null : Pattern.compile(backgroundPattern, Pattern.DOTALL));

        this.name = name;
    }

    public boolean matchPattern(StyledText msg, MessageType messageType) {
        Pattern pattern = (messageType == MessageType.FOREGROUND ? foregroundPattern : backgroundPattern);
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
