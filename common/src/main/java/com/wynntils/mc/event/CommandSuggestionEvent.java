/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.neoforged.bus.api.Event;

public abstract class CommandSuggestionEvent extends Event {
    private final String input;
    private final List<String> suggestions;

    protected CommandSuggestionEvent(String input, List<String> suggestions) {
        this.input = input;
        this.suggestions = suggestions;
    }

    public String getInput() {
        return input;
    }

    public void addSuggestion(String suggestion) {
        suggestions.add(suggestion);
    }

    public void removeSuggestion(String suggestion) {
        suggestions.remove(suggestion);
    }

    public List<String> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    public static class Add extends CommandSuggestionEvent {
        public Add(String input) {
            super(input, new ArrayList<>());
        }
    }

    public static class Modify extends CommandSuggestionEvent {
        public Modify(String input, List<String> suggestions) {
            super(input, suggestions);
        }
    }
}
