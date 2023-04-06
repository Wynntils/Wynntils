/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.type;

import com.wynntils.core.text.StyledText2;
import java.util.regex.Pattern;

public enum RecipientType {
    INFO(null, null, "Info"),
    CLIENTSIDE(null, null, "Clientside"),
    // https://regexr.com/7b14s
    NPC(
            "^(?:§r)?§7\\[\\d+\\/\\d+\\](?:§r§.)? ?§r§[25] ?.+: ?§r§..*$",
            "^(?:§r)?§8\\[\\d+\\/\\d+\\] .+: ?§r§..*$",
            "NPC"),
    GLOBAL(
            "^§8\\[(Lv\\. )?\\d+\\*?/\\d+/..(/[^]]+)?\\]§r§7 \\[[A-Z0-9]+\\]§r.*$",
            "^(§r§8)?\\[(Lv\\. )?\\d+\\*?/\\d+/..(/[^]]+)?\\] \\[[A-Z0-9]+\\](§r§7)?( \\[(§k\\|)?§r§.[A-Z+]+§r§.(§k\\|§r§7)?\\])?(§r§7)? (§r§8)?.*$",
            "Global"),
    LOCAL(
            "^§.\\[(Lv. )?\\d+\\*?/\\d+/..(/[^]]+)?\\]§r.*$",
            "^(§r§8)?\\[(Lv. )?\\d+\\*?/\\d+/..(/[^]]+)?\\]( \\[(§k\\|)?§r§.[A-Z+]+§r§.(§k\\|§r§7)?\\])?(§r§7)? (§r§8)?.*$",
            "Local"),
    GUILD("^(§r)?§3\\[(§b★{0,5}§3)?.*§3]§. .*$", "^(§r§8)?\\[(§r§7★{0,5}§r§8)?.*]§r§7 .*$", "Guild"),
    PARTY("^§7\\[§r§e[^➤]*§r§7\\] §r§f.*$", "^(§r§8)?\\[§r§7[^➤]*§r§8\\] §r§7[^§]*$", "Party"),
    PRIVATE("^§7\\[.* ➤ .*\\] §r§f.*$", "^(§r§8)?\\[.* ➤ .*\\] §r§7.*$", "Private"),
    SHOUT("^§3.* \\[[A-Z0-9]+\\] shouts: §r§b.*$", "^(§r§8)?.* \\[[A-Z0-9]+\\] shouts: §r§7.*$", "Shout"),
    PETS("^§2(.*): §r§a(.*)$", "^§8(.*): §r§7(.*)$", "Pets");

    private final Pattern foregroundPattern;
    private final Pattern backgroundPattern;
    private final String name;

    RecipientType(String foregroundPattern, String backgroundPattern, String name) {
        this.foregroundPattern = (foregroundPattern == null ? null : Pattern.compile(foregroundPattern));
        this.backgroundPattern = (backgroundPattern == null ? null : Pattern.compile(backgroundPattern));

        this.name = name;
    }

    public boolean matchPattern(StyledText2 msg, MessageType messageType) {
        Pattern pattern = (messageType == MessageType.FOREGROUND ? foregroundPattern : backgroundPattern);
        if (pattern == null) return false;
        return msg.getMatcher(pattern).find();
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
