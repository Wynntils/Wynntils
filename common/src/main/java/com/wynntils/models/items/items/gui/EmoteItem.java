/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmoteItem extends GuiItem {
    private static final Pattern EMOTE_STRING_PATTERN =
            Pattern.compile("EmoteItem\\{emoteName=([\\w\\s_-]+), emoteCommand=([\\w_-]+)}");
    private final String emoteName;
    private final String emoteCommand;

    public EmoteItem(String emoteName, String emoteCommand) {
        this.emoteName = emoteName;
        this.emoteCommand = emoteCommand;
    }

    public String getEmoteName() {
        return emoteName;
    }

    public String getEmoteCommand() {
        return emoteCommand;
    }

    @Override
    public String toString() {
        return "EmoteItem{" + "emoteName=" + emoteName + ", emoteCommand=" + emoteCommand + '}';
    }

    public static EmoteItem fromString(String string) {
        if (string == null) return null;
        Matcher matcher = EMOTE_STRING_PATTERN.matcher(string);
        if (!matcher.matches()) return null;

        return new EmoteItem(matcher.group(1), matcher.group(2));
    }
}
