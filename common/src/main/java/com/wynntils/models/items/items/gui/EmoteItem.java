/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

public class EmoteItem extends GuiItem {
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
}
