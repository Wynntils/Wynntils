/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.chat.type.RecipientType;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ChatTab {
    private final String name;
    private final boolean consuming;
    private final String autoCommand;

    // Filters
    private final Set<RecipientType> filteredTypes;
    private final String customRegexString;
    private transient Pattern customRegex;

    public ChatTab(
            String name,
            boolean consuming,
            String autoCommand,
            Set<RecipientType> filteredTypes,
            String customRegexString) {
        this.name = name;
        this.consuming = consuming;
        this.autoCommand = autoCommand;
        this.filteredTypes = filteredTypes;
        this.customRegexString = customRegexString;
    }

    public String getName() {
        return name;
    }

    public boolean isConsuming() {
        return consuming;
    }

    public String getAutoCommand() {
        return autoCommand;
    }

    public Optional<Pattern> getCustomRegex() {
        if (customRegex == null) {
            customRegex = compileRegex(customRegexString);
        }

        return Optional.ofNullable(customRegex);
    }

    public String getCustomRegexString() {
        return customRegexString;
    }

    public Set<RecipientType> getFilteredTypes() {
        return filteredTypes;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ChatTab chatTab = (ChatTab) other;
        return consuming == chatTab.consuming
                && Objects.equals(name, chatTab.name)
                && Objects.equals(filteredTypes, chatTab.filteredTypes)
                && Objects.equals(customRegexString, chatTab.customRegexString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, consuming, filteredTypes, customRegexString);
    }

    private Pattern compileRegex(String customRegexString) {
        if (customRegexString != null && !customRegexString.isBlank()) {
            try {
                return Pattern.compile(customRegexString, Pattern.DOTALL);
            } catch (PatternSyntaxException e) {
                WynntilsMod.warn("Got a saved invalid chat tab regex: " + customRegexString);
                return null;
            }
        }
        return null;
    }
}
