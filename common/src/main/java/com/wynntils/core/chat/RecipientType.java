package com.wynntils.core.chat;

import java.util.regex.Pattern;

public enum RecipientType {
    INFO(null, null),
    GLOBAL(
            "^§8\\[(Lv\\. )?\\d+\\*?/\\d+/..(/[^]]+)?\\]§r§7 \\[[A-Z0-9]+\\]§r.*$",
            "^(§r§8)?\\[(Lv\\. )?\\d+\\*?/\\d+/..(/[^]]+)?\\] \\[[A-Z0-9]+\\](§r§7)?( \\[(§k\\|)?§r§.[A-Z+]+§r§.(§k\\|§r§7)?\\])?(§r§7)? (§r§8)?.*$"),
    LOCAL(
            "^§7\\[(Lv. )?\\d+\\*?/\\d+/..(/[^]]+)\\]§r.*$",
            "^(§r§8)?\\[(Lv. )?\\d+\\*?/\\d+/..(/[^]]+)\\]( \\[(§k\\|)?§r§.[A-Z+]+§r§.(§k\\|§r§7)?\\])?(§r§7)? (§r§8)?.*$"),
    GUILD("^(§r)?§3\\[(§b★+§3)?.*§3]§. .*$", "^(§r§8)?\\[(§r§7★+§r§8)?.*]§r§7 .*$"),
    PARTY("^§7\\[§r§e.*§r§7\\] §r§f.*$", "^(§r§8)?\\[§r§7.*§r§8\\] §r§7.*$"),
    PRIVATE("^§7\\[§r§5.*§r§6 ➤ §r§5.*§r§7\\] §r§f.*$", "^(§r§8)?\\[.* ➤ .*\\] §r§7.*$"),
    SHOUT("^§3.* \\[[A-Z0-9]+\\] shouts: §r§b.*$", "^(§r§8)?.* \\[[A-Z0-9]+\\] shouts: §r§7.*$");

    private final Pattern normalPattern;
    private final Pattern backgroundPattern;

    RecipientType(String normalPattern, String backgroundPattern) {
        this.normalPattern = (normalPattern == null ? null : Pattern.compile(normalPattern));
        this.backgroundPattern = (backgroundPattern == null ? null : Pattern.compile(backgroundPattern));
    }

    public boolean matchPattern(String msg, MessageType type) {
        assert (type == MessageType.NORMAL
                || type == MessageType.BACKGROUND);
        Pattern pattern = (type == MessageType.NORMAL ? normalPattern : backgroundPattern);
        if (pattern == null) return false;
        return pattern.matcher(msg).find();
    }
}
