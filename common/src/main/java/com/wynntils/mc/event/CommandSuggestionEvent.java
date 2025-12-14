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
    private final List<String> suggestions;

    protected CommandSuggestionEvent(List<String> suggestions) {
        this.suggestions = suggestions;
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

    public static final class Add extends CommandSuggestionEvent {
        private final String input;

        public Add(String input) {
            super(new ArrayList<>());

            this.input = input;
        }

        public String getInput() {
            return input;
        }
    }

    public static final class Modify extends CommandSuggestionEvent {
        public Modify(List<String> suggestions) {
            super(suggestions);
        }
    }
}
