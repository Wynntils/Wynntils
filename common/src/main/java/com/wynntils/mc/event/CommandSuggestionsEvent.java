/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.brigadier.suggestion.Suggestions;
import com.wynntils.core.events.EventThread;
import net.neoforged.bus.api.Event;

@EventThread(EventThread.Type.ANY)
public class CommandSuggestionsEvent extends Event {
    private final int id;
    private final Suggestions suggestions;

    public CommandSuggestionsEvent(int id, Suggestions suggestions) {
        this.id = id;
        this.suggestions = suggestions;
    }

    public int getId() {
        return id;
    }

    public Suggestions getSuggestions() {
        return suggestions;
    }
}
