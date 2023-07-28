/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.type;

import com.wynntils.core.text.StyledText;
import java.util.regex.Pattern;

public enum RecipientType {
    INFO(null, null, "Info"),
    CLIENTSIDE(null, null, "Clientside"),
    // https://regexr.com/7b14s
    NPC("^§7\\[\\d+\\/\\d+\\](?:§.)? ?§[25] ?.+: ?§..*$", "^§8\\[\\d+\\/\\d+\\] .+: ?§..*$", "NPC"),
    GLOBAL(
            "^§8\\[(Lv\\. )?\\d+\\*?/\\d+/..(/[^]]+)?\\]§7 \\[[A-Z0-9]+\\].*$",
            "^(§8)?\\[(Lv\\. )?\\d+\\*?/\\d+/..(/[^]]+)?\\] \\[[A-Z0-9]+\\](§7)?( \\[(§k\\|§r)?§.[A-Z+]+§.(§k\\|§r§7)?\\])?(§7)? (§8)?.*$",
            "Global"),
    LOCAL(
            "^§.\\[(Lv. )?\\d+\\*?/\\d+/..(/[^]]+)?\\].*$",
            "^(§8)?\\[(Lv. )?\\d+\\*?/\\d+/..(/[^]]+)?\\]( \\[(§k\\|§r)?§.[A-Z+]+.(§k\\|§r§7)?\\])?(§7)? (§8)?.*$",
            "Local"),
    // https://regexr.com/7f8ma
    GUILD(
            "^§3\\[(§b)?(★{0,5})?(§3)?(§o)?(?!INFO).+(§3)?](§.)? .+$",
            "^(§8)?\\[(§7)?(★{0,5})?(§8)?(§o)?(?!INFO).+]§7 .+$",
            "Guild"),
    PARTY("^§7\\[§e[^➤]*§7\\] §f.*$", "^(§8)?\\[§7[^➤]*§8\\] §7[^§]*$", "Party"),
    PRIVATE("^§7\\[.* ➤ .*\\] §f.*$", "^(§8)?\\[.* ➤ .*\\] §7.*$", "Private"),
    SHOUT("^§5.* \\[[A-Z0-9]+\\] shouts: §d.*$", "^(§8)?.* \\[[A-Z0-9]+\\] shouts: §7.*$", "Shout"),
    PETS("^§2(.*): §a(.*)$", "^§8(.*): §7(.*)$", "Pets");

    private final Pattern foregroundPattern;
    private final Pattern backgroundPattern;
    private final String name;

    RecipientType(String foregroundPattern, String backgroundPattern, String name) {
        this.foregroundPattern = (foregroundPattern == null ? null : Pattern.compile(foregroundPattern));
        this.backgroundPattern = (backgroundPattern == null ? null : Pattern.compile(backgroundPattern));

        this.name = name;
    }

    public boolean matchPattern(StyledText msg, MessageType messageType) {
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
