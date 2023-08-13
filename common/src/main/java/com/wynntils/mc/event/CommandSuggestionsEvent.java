/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import net.minecraftforge.eventbus.api.Event;

public class CommandSuggestionsEvent extends Event {
    private CompletableFuture<Suggestions> suggestions;
    private final StringReader command;
    private final int cursor;

    public CommandSuggestionsEvent(CompletableFuture<Suggestions> suggestions, StringReader command, int cursor) {
        this.suggestions = suggestions;
        this.command = command;
        this.cursor = cursor;
    }

    public CompletableFuture<Suggestions> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(CompletableFuture<Suggestions> suggestions) {
        this.suggestions = suggestions;
    }

    public StringReader getCommand() {
        return command;
    }

    public int getCursor() {
        return cursor;
    }
}
