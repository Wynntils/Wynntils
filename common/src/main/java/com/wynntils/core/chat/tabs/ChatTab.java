/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.tabs;

import com.wynntils.core.chat.RecipientType;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

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

    public boolean matchMessageFromEvent(ChatMessageReceivedEvent event) {
        if (filteredTypes != null && !filteredTypes.isEmpty() && !filteredTypes.contains(event.getRecipientType())) {
            return false;
        }

        if (customRegexString != null
                && !customRegexString.isBlank()
                && !getCustomRegex().matcher(event.getOriginalCodedMessage()).matches()) {
            return false;
        }

        return true;
    }

    public boolean matchMessageFromEvent(ClientsideMessageEvent event) {
        if (filteredTypes != null && !filteredTypes.isEmpty() && !filteredTypes.contains(RecipientType.CLIENTSIDE)) {
            return false;
        }

        if (customRegexString == null) {
            return true;
        }

        return getCustomRegex().matcher(event.getCodedMessage()).matches();
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

    /** This is only allowed to be called if customRegexString != null.
     */
    private Pattern getCustomRegex() {
        assert (customRegexString != null);

        if (customRegex == null) {
            customRegex = Pattern.compile(customRegexString, Pattern.DOTALL);
        }
        return customRegex;
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
}
